package com.example.core.domain.order_product;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.order.Order;
import com.example.core.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Long productPrice;

    public static OrderProduct createOrderProduct(Order order, Product product, Long quantity, Long price) {
        return OrderProduct.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .productPrice(price)
                .build();
    }
}
