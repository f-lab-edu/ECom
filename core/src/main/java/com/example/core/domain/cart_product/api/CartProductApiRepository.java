package com.example.core.domain.cart_product.api;

import com.example.core.domain.cart_product.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartProductApiRepository extends JpaRepository<CartProduct, Long> {

    List<CartProduct> findByCartId(Long cartId);
    Optional<CartProduct> findByCart_IdAndProduct_Id(Long cartId, Long productId);
}
