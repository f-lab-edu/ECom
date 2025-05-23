package com.example.core.dto;

import com.example.core.enums.PaymentMehtod;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductPaymentRequestDto {
    private Long userId;
    private Long productId;
    private Long quantity;
    private PaymentMehtod paymentMethod;

    public static ProductPaymentRequestDto of(Long userId, Long productId, Long quantity, PaymentMehtod paymentMethod) {
        return ProductPaymentRequestDto.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .paymentMethod(paymentMethod)
                .build();
    }
}
