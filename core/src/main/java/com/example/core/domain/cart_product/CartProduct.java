package com.example.core.domain.cart_product;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Entity
@Table
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartProduct extends BaseEntity {

    @EmbeddedId
    private CartProductId id;

    @MapsId("cartId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Long quantity;

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartProductId implements Serializable {
        private Long cartId;
        private Long productId;
    }

    public static CartProduct of(Cart cart, Product product, Long quantity) {
        return builder()
                .id(new CartProductId(cart.getId(), product.getId()))
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
    }

    public void addQuantity(Long quantity) {
        this.quantity += quantity;
    }
}
