package com.example.api.module.coupon.service;

import com.example.core.domain.coupon.Coupon;
import com.example.core.domain.coupon.api.CouponApiRepository;
import com.example.core.enums.CouponStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class CouponService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final CouponApiRepository couponApiRepository;

    public boolean tryReserve(Long userId, Long orderId, Long couponId) {
        Coupon coupon = couponApiRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("coupon not found"));

        // 쿠폰 사용이 불가하거나, 사용자가 쿠폰을 소유하지 않은 경우
        if (coupon.getStatus() != CouponStatus.AVAILABLE || !coupon.getUser().getId().equals(userId)) {
            log.warn("Coupon is not available or user does not own the coupon: userId={}, couponId={}", userId, couponId);
            return false;
        }

        String lockKey = "lock:coupon:" + couponId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 최대 3초 동안 대기. 락을 얻으면 10초 동안 유지.
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                log.warn("쿠폰 락 획득 실패. lockKey: {}", lockKey);
                return false;
            }

            String reservationKey = "coupon:reservations:" + couponId;
            // 이 쿠폰에 대한 예약이 하나라도 존재하면 실패 처리
            if (redisTemplate.opsForHash().size(reservationKey) > 0) {
                return false;
            }

            // 예약이 없으면 현재 주문 정보로 예약
            redisTemplate.opsForHash().put(reservationKey, String.valueOf(orderId), "1"); // 값은 1로 통일
            redisTemplate.expire(reservationKey, 24, TimeUnit.HOURS);
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void revertReservation(Long orderId, Long couponId) {
        String lockKey = "lock:coupon:" + couponId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            lock.lock(); // 롤백은 중요한 작업이므로 락을 확실히 획득
            String reservationKey = "coupon:reservations:" + couponId;
            // '내 주문'이 예약한게 맞는지 확인하고 삭제
            if (redisTemplate.opsForHash().hasKey(reservationKey, String.valueOf(orderId))) {
                redisTemplate.opsForHash().delete(reservationKey, String.valueOf(orderId));
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public void confirmCoupon(Long couponId) {
        // Redisson 분산 락 획득
        RLock lock = redissonClient.getLock("lock:coupon:" + couponId);

        try {
            lock.lock();

            // DB 비관적 락 획득 및 데이터 조회
            Coupon coupon = couponApiRepository.findByIdWithPessimisticLock(couponId)
                    .orElseThrow(() -> new IllegalStateException("cannot find coupon: " + couponId));

            // 쿠폰 상태 최종 확인 및 변경
            if (coupon.getStatus() != CouponStatus.AVAILABLE) {
                throw new IllegalStateException("already used or not available coupon: " + couponId);
            }
            coupon.use();

        } finally {
            // Redisson 분산 락 해제
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
