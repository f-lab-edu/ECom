package com.example.core.domain.order;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.order_product.OrderProduct;
import com.example.core.domain.payment.Payment;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.user.User;
import com.example.core.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private ShippingAddress shippingAddress;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = true)
    private Payment payment;

    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order")
    private List<OrderProduct> orderProducts;

    public static Order createOrder(User user, ShippingAddress shippingAddress, Payment payment) {
        return Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .payment(payment)
                .status(OrderStatus.CREATED)
                .build();
    }
}
