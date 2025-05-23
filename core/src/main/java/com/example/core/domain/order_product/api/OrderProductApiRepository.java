package com.example.core.domain.order_product.api;

import com.example.core.domain.order_product.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductApiRepository extends JpaRepository<OrderProduct, Long> {

}
