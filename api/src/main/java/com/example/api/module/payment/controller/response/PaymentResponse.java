package com.example.api.module.payment.controller.response;

import com.example.core.domain.payment.Payment;
import com.example.core.dto.OrderDto;
import com.example.core.enums.PaymentMehtod;
import com.example.core.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private OrderDto order;
    private PaymentStatus paymentStatus;
    private PaymentMehtod paymentMehtod;
    private Long paymentAmount;

    public static PaymentResponse of(Long paymentId, OrderDto order, PaymentStatus paymentStatus, PaymentMehtod paymentMehtod, Long paymentAmount) {
        return PaymentResponse.builder()
                .paymentId(paymentId)
                .order(order)
                .paymentStatus(paymentStatus)
                .paymentMehtod(paymentMehtod)
                .paymentAmount(paymentAmount)
                .build();
    }

    public static PaymentResponse of(Payment payment, OrderDto order) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .order(order)
                .paymentStatus(payment.getPaymentStatus())
                .paymentMehtod(payment.getPaymentMethod())
                .paymentAmount(payment.getPaymentAmount())
                .build();
    }
}
