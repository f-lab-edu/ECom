package com.example.api.module.order.controller.request;

import com.example.core.dto.OrderProductRequestDto;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderProductRequest {
    private List<OrderProductRequestDto> orderProducts;
    private Long shippingAddressId;
}

