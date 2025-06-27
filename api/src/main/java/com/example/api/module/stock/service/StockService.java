package com.example.api.module.stock.service;

import com.example.core.domain.product.Product;
import com.example.core.domain.product.api.ProductApiRepository;
import com.example.core.dto.OrderProductRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class StockService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final ProductApiRepository productApiRepository;

    /**
     * 재고를 예약하는 메소드. 성공하면 true, 실패하면 false 반환
     * @param orderId 주문 ID
     * @param productId 상품 ID
     * @param quantity 예약할 수량
     * @return 예약 성공 여부
     **/
    public boolean tryReserve(Long orderId, Long productId, Long quantity) {
        String lockKey = "lock:product:" + productId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 최대 3초 동안 대기. 락을 얻으면 10초 동안 유지.
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                log.warn("재고 락 획득 실패. lockKey: {}", lockKey);
                return false;
            }

            // 1. DB에서 현재 구매 가능한 재고를 가져온다. (Source of Truth)
            Long dbAvailable = productApiRepository.findById(productId)
                    .map(Product::getStockQuantity)
                    .orElse(0L);

            // 2. Redis에서 이 상품에 대해 현재 예약된 모든 수량의 합을 구한다.
            String reservationKey = "product:reservations:" + productId;
            Map<Object, Object> reservations = redisTemplate.opsForHash().entries(reservationKey);
            long totalReserved = reservations.values().stream()
                    .mapToLong(val -> Long.parseLong(val.toString()))
                    .sum();

            // 3. 실제 예약 가능한 재고 = DB 재고 - 현재 Redis에 예약된 총량
            long effectiveAvailable = dbAvailable - totalReserved;

            // 4. 예약 가능한지 확인
            if ((dbAvailable - totalReserved) >= quantity) {
                redisTemplate.opsForHash().put(reservationKey, String.valueOf(orderId), String.valueOf(quantity));
                redisTemplate.expire(reservationKey, 24, TimeUnit.HOURS);
                return true;
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            // 락을 항상 반환해야함.
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 재고를 복구하는 메소드. 항상 성공해야함
     * @param orderId 주문 ID
     * @param productId 상품 ID
     */
    public void revertReservation(Long orderId, Long productId) {
        String lockKey = "lock:product:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            lock.lock();
            String reservationKey = "product:reservations:" + productId;
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
    public void confirmStock(List<OrderProductRequestDto> orderProductRequestDtos) {
        List<RLock> locks = orderProductRequestDtos.stream()
                .map(dto -> redissonClient.getLock("lock:product:" + dto.getProductId()))
                .toList();

        RLock multiLock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));

        try {
            //모든 락 획득까지 대기
            multiLock.lock();

            for (OrderProductRequestDto dto : orderProductRequestDtos) {
                long productId = dto.getProductId(), quantity = dto.getQuantity();
                Product product = productApiRepository.findByIdWithPessimisticLock(productId)
                        .orElseThrow(() -> new IllegalStateException("Product not found: " + productId));

                // 재고 수량 재확인
                if (product.getStockQuantity() < quantity) {
                    throw new IllegalStateException("Not enough stock for product: " + productId);
                }
                product.decreaseStock(quantity);
            }
        } finally {
            multiLock.unlock();
        }
    }
}
