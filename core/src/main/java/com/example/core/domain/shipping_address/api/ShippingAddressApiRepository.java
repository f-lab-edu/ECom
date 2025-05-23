package com.example.core.domain.shipping_address.api;

import com.example.core.domain.shipping_address.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingAddressApiRepository extends JpaRepository<ShippingAddress, Long> {
    Optional<ShippingAddress> findByIdAndUserId(Long id, Long userId);
    List<ShippingAddress> findAllByUserId(Long userId);
    Long countByUserId(Long userId);
}
