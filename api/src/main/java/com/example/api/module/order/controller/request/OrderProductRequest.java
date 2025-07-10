package com.example.api.module.order.controller.request;

import java.util.List;

import com.example.core.dto.OrderProductRequestDto;
import com.example.core.dto.PaymentRequestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderProductRequest {
    private List<OrderProductRequestDto> orderProductDtos;
    private PaymentRequestDto paymentRequestDto;
    private Long couponId;
    private Long shippingAddressId;
}

