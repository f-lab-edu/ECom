package com.example.core.domain.order.api;

import com.example.core.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderApiRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
}
