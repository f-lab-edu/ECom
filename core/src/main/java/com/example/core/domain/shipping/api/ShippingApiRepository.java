package com.example.core.domain.shipping.api;

import com.example.core.domain.shipping.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingApiRepository extends JpaRepository<Shipping, Long> {
}
