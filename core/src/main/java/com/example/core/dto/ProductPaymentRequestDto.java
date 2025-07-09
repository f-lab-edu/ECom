package com.example.core.dto;

import com.example.core.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductPaymentRequestDto {
    private Long userId;
    private Long productId;
    private Long quantity;
    private PaymentMethod paymentMethod;

    public static ProductPaymentRequestDto of(Long userId, Long productId, Long quantity, PaymentMethod paymentMethod) {
        return ProductPaymentRequestDto.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .paymentMethod(paymentMethod)
                .build();
    }
}
