package com.example.api.module.order.controller.request;

import com.example.core.dto.OrderProductRequestDto;
import com.example.core.dto.PaymentRequestDto;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderProductRequest {
    private List<OrderProductRequestDto> orderProductDtos;
    private PaymentRequestDto paymentRequestDto;
    private Long couponId;
    private Long shippingAddressId;
}

