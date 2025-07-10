package com.example.api.module.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.example.core.domain.category.Category;
import com.example.core.domain.product.Product;
import com.example.core.domain.product.api.ProductApiRepository;
import com.example.core.dto.OrderProductRequestDto;

@SpringBootTest
@ActiveProfiles({"api-test", "core-test"})
@Transactional
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @MockitoBean
    private ProductApiRepository productApiRepository;

    @MockitoBean
    private RedissonClient redissonClient;

    @MockitoBean
    private RLock rLock;
    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;
    @MockitoBean
    private HashOperations<String, Object, Object> hashOperations;

    private Product testProduct;
    private Category testCategory;
    private Long testOrderId = 100L;
    private Long testProductId = 1L;

    @BeforeEach
    void setUp() {
        // 테스트 카테고리 생성
        testCategory = Category.builder()
                .name("Test Category")
                .build();

        // 테스트 상품 생성
        testProduct = Product.builder()
                .productName("Test Product")
                .description("Test Description")
                .stockQuantity(100L)
                .price(10000L)
                .thumbnailUrl("http://test-thumbnail.com")
                .category(testCategory)
                .build();

        // Mock 기본 설정
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    // =========================== tryReserve 테스트 (중요도: 높음) ===========================

    @Test
    void tryReserve_성공() throws InterruptedException {
        // given
        Long requestQuantity = 10L;
        Map<Object, Object> reservations = new HashMap<>(); // 기존 예약 없음

        when(rLock.tryLock(5, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(productApiRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(hashOperations.entries("product:reservations:" + testProductId)).thenReturn(reservations);

        // when
        boolean result = stockService.tryReserve(testOrderId, testProductId, requestQuantity);

        // then
        assertTrue(result);
        verify(hashOperations, times(1)).put("product:reservations:" + testProductId, String.valueOf(testOrderId), String.valueOf(requestQuantity));
        verify(redisTemplate, times(1)).expire("product:reservations:" + testProductId, 24, TimeUnit.HOURS);
        verify(rLock, times(1)).unlock();
    }

    @Test
    void tryReserve_실패_재고부족() throws InterruptedException {
        // given
        Long requestQuantity = 150L; // 재고(100)보다 많은 요청
        Map<Object, Object> reservations = new HashMap<>();

        when(rLock.tryLock(5, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(productApiRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(hashOperations.entries("product:reservations:" + testProductId)).thenReturn(reservations);

        // when
        boolean result = stockService.tryReserve(testOrderId, testProductId, requestQuantity);

        // then
        assertFalse(result);
        verify(hashOperations, never()).put(anyString(), any(), any());
        verify(rLock, times(1)).unlock();
    }

    @Test
    void tryReserve_실패_이미예약된수량고려() throws InterruptedException {
        // given
        Long requestQuantity = 30L;
        Map<Object, Object> reservations = new HashMap<>();
        reservations.put("80", "80"); // 기존 예약 80개
        // 실제 가능한 재고: 100 - 80 = 20개, 요청 30개는 불가능

        when(rLock.tryLock(5, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(productApiRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(hashOperations.entries("product:reservations:" + testProductId)).thenReturn(reservations);

        // when
        boolean result = stockService.tryReserve(testOrderId, testProductId, requestQuantity);

        // then
        assertFalse(result);
        verify(hashOperations, never()).put(anyString(), any(), any());
        verify(rLock, times(1)).unlock();
    }

    // =========================== revertReservation 테스트 (중요도: 높음) ===========================

    @Test
    void revertReservation_성공() {
        // given
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(hashOperations.hasKey("product:reservations:" + testProductId, String.valueOf(testOrderId))).thenReturn(true);

        // when
        stockService.revertReservation(testOrderId, testProductId);

        // then
        verify(rLock, times(1)).lock();
        verify(hashOperations, times(1)).delete("product:reservations:" + testProductId, String.valueOf(testOrderId));
        verify(rLock, times(1)).unlock();
    }

    @Test
    void revertReservation_성공_예약없음() {
        // given
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(hashOperations.hasKey("product:reservations:" + testProductId, String.valueOf(testOrderId))).thenReturn(false);

        // when
        stockService.revertReservation(testOrderId, testProductId);

        // then
        verify(rLock, times(1)).lock();
        verify(hashOperations, never()).delete(anyString(), any());
        verify(rLock, times(1)).unlock();
    }

    // =========================== confirmStock 테스트 (중요도: 높음) ===========================

    @Test
    void confirmStock_성공() {
        // given
        OrderProductRequestDto dto = createOrderProductRequestDto(testProductId, 10L);
        List<OrderProductRequestDto> dtos = Collections.singletonList(dto);
        RLock[] locks = {rLock};
        RLock multiLock = mock(RLock.class);

        when(redissonClient.getMultiLock(any(RLock[].class))).thenReturn(multiLock);
        when(productApiRepository.findByIdWithPessimisticLock(testProductId)).thenReturn(Optional.of(testProduct));

        // when
        stockService.confirmStock(dtos);

        // then
        verify(multiLock, times(1)).lock();
        verify(multiLock, times(1)).unlock();
        verify(productApiRepository, times(1)).findByIdWithPessimisticLock(testProductId);
    }

    @Test
    void confirmStock_실패_상품없음() {
        // given
        OrderProductRequestDto dto = createOrderProductRequestDto(testProductId, 10L);
        List<OrderProductRequestDto> dtos = Collections.singletonList(dto);
        RLock[] locks = {rLock};
        RLock multiLock = mock(RLock.class);

        when(redissonClient.getMultiLock(any(RLock[].class))).thenReturn(multiLock);
        when(productApiRepository.findByIdWithPessimisticLock(testProductId)).thenReturn(Optional.empty());

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> stockService.confirmStock(dtos));
        assertEquals("Product not found: " + testProductId, exception.getMessage());

        verify(multiLock, times(1)).lock();
        verify(multiLock, times(1)).unlock();
    }

    @Test
    void confirmStock_실패_재고부족() {
        // given
        OrderProductRequestDto dto = createOrderProductRequestDto(testProductId, 150L); // 재고보다 많은 요청
        List<OrderProductRequestDto> dtos = Collections.singletonList(dto);
        RLock[] locks = {rLock};
        RLock multiLock = mock(RLock.class);

        when(redissonClient.getMultiLock(any(RLock[].class))).thenReturn(multiLock);
        when(productApiRepository.findByIdWithPessimisticLock(testProductId)).thenReturn(Optional.of(testProduct));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> stockService.confirmStock(dtos));
        assertEquals("Not enough stock for product: " + testProductId, exception.getMessage());

        verify(multiLock, times(1)).lock();
        verify(multiLock, times(1)).unlock();
    }

    // Helper method
    private OrderProductRequestDto createOrderProductRequestDto(Long productId, Long quantity) {
        OrderProductRequestDto dto = new OrderProductRequestDto();
        try {
            java.lang.reflect.Field productIdField = OrderProductRequestDto.class.getDeclaredField("productId");
            productIdField.setAccessible(true);
            productIdField.set(dto, productId);
            
            java.lang.reflect.Field quantityField = OrderProductRequestDto.class.getDeclaredField("quantity");
            quantityField.setAccessible(true);
            quantityField.set(dto, quantity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test DTO", e);
        }
        return dto;
    }
} 