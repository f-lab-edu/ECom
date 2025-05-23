package com.example.core.domain.payment;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.order.Order;
import com.example.core.enums.PaymentMehtod;
import com.example.core.enums.PaymentStatus;
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
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "payment", fetch = FetchType.LAZY)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column
    private PaymentMehtod paymentMethod;

    @Column
    private Long paymentAmount;

    public static Payment createPayment(PaymentMehtod paymentMethod, Long paymentAmount, PaymentStatus paymentStatus) {
        return Payment.builder()
                .paymentStatus(paymentStatus)
                .paymentMethod(paymentMethod)
                .paymentAmount(paymentAmount)
                .build();
    }
}
