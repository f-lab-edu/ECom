package com.example.core.dto;

import com.example.core.enums.PaymentMethod;
import lombok.Getter;

@Getter
public class PaymentRequestDto {
    private String cardNumber;
    private Long paymentAmount;
    private PaymentMethod paymentMethod;
}
