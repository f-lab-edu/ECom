package com.example.api.module.coupon.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.core.domain.cart.Cart;
import com.example.core.domain.coupon.Coupon;
import com.example.core.domain.coupon.api.CouponApiRepository;
import com.example.core.domain.order.api.OrderApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.enums.CouponStatus;

@SpringBootTest
@ActiveProfiles({"api-test", "core-test"})
@Transactional
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @MockitoBean
    private CouponApiRepository couponApiRepository;

    @MockitoBean
    private UserApiRepository userApiRepository;

    @MockitoBean
    private OrderApiRepository orderApiRepository;
    @MockitoBean
    private RedissonClient redissonClient;
    @MockitoBean
    private RLock rLock;
    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;
    @MockitoBean
    private HashOperations<String, Object, Object> hashOperations;


    private User testUser;
    private Coupon testCoupon;
    private Long testUserId = 1L;
    private Long testOrderId = 100L;
    private Long testCouponId = 10L;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        Cart cart = new Cart();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .nickname("Test User")
                .salt("testSalt")
                .hashedPassword("hashedPassword")
                .phoneNumber("010-1234-5678")
                .cart(cart)
                .build();

        // 테스트 쿠폰 생성
        testCoupon = Coupon.builder()
                .id(testCouponId)
                .couponCode("TEST-COUPON-001")
                .amount(5000L)
                .status(CouponStatus.AVAILABLE)
                .user(testUser)
                .build();

        // Mock 기본 설정
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    // =========================== tryReserve 테스트 (중요도: 높음) ===========================

    @Test
    void tryReserve_성공() throws InterruptedException {
        // given
        when(couponApiRepository.findById(testCouponId)).thenReturn(Optional.of(testCoupon));
        when(rLock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(hashOperations.size("coupon:reservations:" + testCouponId)).thenReturn(0L);

        // when
        boolean result = couponService.tryReserve(testUserId, testOrderId, testCouponId);

        // then
        assertTrue(result);
        verify(hashOperations, times(1)).put("coupon:reservations:" + testCouponId, String.valueOf(testOrderId), "1");
        verify(redisTemplate, times(1)).expire("coupon:reservations:" + testCouponId, 24, TimeUnit.HOURS);
        verify(rLock, times(1)).unlock();
    }

    @Test
    void tryReserve_실패_사용불가쿠폰() {
        // given
        testCoupon = Coupon.builder()
                .couponCode("USED-COUPON-001")
                .amount(5000L)
                .status(CouponStatus.USED)
                .user(testUser)
                .build();
        when(couponApiRepository.findById(testCouponId)).thenReturn(Optional.of(testCoupon));

        // when
        boolean result = couponService.tryReserve(testUserId, testOrderId, testCouponId);

        // then
        assertFalse(result);
        verify(redissonClient, never()).getLock(anyString());
    }

    @Test
    void tryReserve_실패_이미예약된쿠폰() throws InterruptedException {
        // given
        when(couponApiRepository.findById(testCouponId)).thenReturn(Optional.of(testCoupon));
        when(rLock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(hashOperations.size("coupon:reservations:" + testCouponId)).thenReturn(1L); // 이미 예약 존재

        // when
        boolean result = couponService.tryReserve(testUserId, testOrderId, testCouponId);

        // then
        assertFalse(result);
        verify(hashOperations, never()).put(anyString(), anyString(), anyString());
        verify(rLock, times(1)).unlock();
    }

    // =========================== revertReservation 테스트 (중요도: 높음) ===========================

    @Test
    void revertReservation_성공() {
        // given
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(hashOperations.hasKey("coupon:reservations:" + testCouponId, String.valueOf(testOrderId))).thenReturn(true);

        // when
        couponService.revertReservation(testOrderId, testCouponId);

        // then
        verify(rLock, times(1)).lock();
        verify(hashOperations, times(1)).delete("coupon:reservations:" + testCouponId, String.valueOf(testOrderId));
        verify(rLock, times(1)).unlock();
    }

    @Test
    void revertReservation_성공_예약없음() {
        // given
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(hashOperations.hasKey("coupon:reservations:" + testCouponId, String.valueOf(testOrderId))).thenReturn(false);

        // when
        couponService.revertReservation(testOrderId, testCouponId);

        // then
        verify(rLock, times(1)).lock();
        verify(hashOperations, never()).delete(anyString(), any());
        verify(rLock, times(1)).unlock();
    }

    // =========================== confirmCoupon 테스트 (중요도: 높음) ===========================

    @Test
    void confirmCoupon_성공() {
        // given
        when(rLock.isLocked()).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(couponApiRepository.findByIdWithPessimisticLock(testCouponId)).thenReturn(Optional.of(testCoupon));

        // when
        couponService.confirmCoupon(testCouponId);

        // then
        verify(rLock, times(1)).lock();
        verify(couponApiRepository, times(1)).findByIdWithPessimisticLock(testCouponId);
        verify(rLock, times(1)).unlock();
        // 쿠폰 사용 상태 변경 확인은 별도 메소드로 검증 필요
    }

    @Test
    void confirmCoupon_실패_쿠폰없음() {
        // given
        when(rLock.isLocked()).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(couponApiRepository.findByIdWithPessimisticLock(testCouponId)).thenReturn(Optional.empty());

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> couponService.confirmCoupon(testCouponId));
        assertTrue(exception.getMessage().contains("cannot find coupon"));
        verify(rLock, times(1)).unlock();
    }

    @Test
    void confirmCoupon_실패_이미사용된쿠폰() {
        // given
        testCoupon = Coupon.builder()
                .couponCode("USED-COUPON-002")
                .amount(5000L)
                .status(CouponStatus.USED)
                .user(testUser)
                .build();

        when(rLock.isLocked()).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(couponApiRepository.findByIdWithPessimisticLock(testCouponId)).thenReturn(Optional.of(testCoupon));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> couponService.confirmCoupon(testCouponId));
        assertTrue(exception.getMessage().contains("already used or not available coupon"));
        verify(rLock, times(1)).unlock();
    }
} 