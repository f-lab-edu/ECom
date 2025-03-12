package com.example.core.domain.cart.api;

import com.example.core.domain.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartApiRepository extends JpaRepository<Cart, Long> {

}
