package com.example.core.domain.order;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.order_product.OrderProduct;
import com.example.core.domain.payment.Payment;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.user.User;
import com.example.core.dto.OrderProductRequestDto;
import com.example.core.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
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

    @OneToOne(mappedBy = "order")
    private Payment payment;

    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderProduct> orderProducts = new ArrayList<>();

    // 주문 시점의 정보를 복사해서 저장
    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 20)
    private String zipCode;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    public static Order of(User user, ShippingAddress shippingAddress) {
        return Order.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .recipientName(shippingAddress.getRecipientName())
                .address(shippingAddress.getAddress())
                .zipCode(shippingAddress.getZipCode())
                .phoneNumber(shippingAddress.getPhoneNumber())
                .build();
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        orderProducts.add(orderProduct);
        orderProduct.setOrder(this);
    }
}
