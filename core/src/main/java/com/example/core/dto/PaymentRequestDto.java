package com.example.core.dto;

import com.example.core.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDto {
    private String cardNumber;
    private Long paymentAmount;
    private PaymentMethod paymentMethod;
}
