package com.example.core.domain.coupon;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.order.Order;
import com.example.core.domain.user.User;
import com.example.core.enums.CouponStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column
    private String couponCode;

    @Column
    private Long amount;

    @Column
    private Long discountPercentage;

    @Enumerated(EnumType.STRING)
    @Column
    private CouponStatus status;

    @Column
    private LocalDateTime expireTime;

    @Column
    private LocalDateTime reservedAt;

    public void use() {
        this.status = CouponStatus.USED;
        this.reservedAt = LocalDateTime.now();
    }
}
