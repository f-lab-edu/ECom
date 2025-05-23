package com.example.api.module.payment.controller.request;

import com.example.core.enums.PaymentMehtod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PaymentRequest {
    @NotNull(message = "orderId is required")
    Long orderId;

    @NotNull(message = "paymentMethod is required")
    PaymentMehtod paymentMethod;

    @NotNull(message = "paymentAmount is required")
    Long paymentAmount;
}
