package com.example.api.module.order.controller.response;

import com.example.core.domain.order.Order;
import com.example.core.domain.order_product.OrderProduct;
import com.example.core.dto.OrderDto;
import com.example.core.dto.OrderProductDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderProductResponse {
    Long userId;
    List<OrderDto> orderDtos;
    List<OrderProductDto> orderProductDtos;

    public static OrderProductResponse of(Long userId, List<Order> orders, List<OrderProduct> orderProducts) {
        return OrderProductResponse.builder()
                .userId(userId)
                .orderDtos(OrderDto.from(orders))
                .orderProductDtos(OrderProductDto.from(orderProducts))
                .build();
    }
}
