package com.example.core.domain.payment;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.order.Order;
import com.example.core.dto.PaymentRequestDto;
import com.example.core.enums.PaymentMethod;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column
    private PaymentMethod paymentMethod;

    @Column
    private String cardNumber;

    @Column
    private String transactionId;

    @Column
    private Long paymentAmount;

    public static Payment create(Order order, PaymentRequestDto paymentRequestDto) {
        return Payment.builder()
                .order(order)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(paymentRequestDto.getPaymentMethod())
                .cardNumber(paymentRequestDto.getCardNumber())
                .paymentAmount(paymentRequestDto.getPaymentAmount())
                .build();
    }

    public void setStatus(PaymentStatus status) {
        this.paymentStatus = status;
    }
}
