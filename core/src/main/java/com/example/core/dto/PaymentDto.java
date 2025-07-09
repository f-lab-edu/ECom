package com.example.core.dto;

import com.example.core.domain.payment.Payment;
import com.example.core.enums.PaymentMethod;
import com.example.core.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentDto {
    private Long id;
    private Long orderId;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String cardNumber;
    private String transactionId;
    private Long paymentAmount;
    
    
    
    public static PaymentDto from(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .paymentStatus(payment.getPaymentStatus())
                .paymentMethod(payment.getPaymentMethod())
                .cardNumber(payment.getCardNumber())
                .transactionId(payment.getTransactionId())
                .paymentAmount(payment.getPaymentAmount())
                .build();
    }
}
