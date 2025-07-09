package com.example.core.dto;

import com.example.core.domain.order.Order;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderDto {
    private Long orderId;
    private ShippingAddressResponseDto shippingAddressResponseDto;
    private Long paymentId;
    private OrderStatus status;

    public static OrderDto from(Order order) {
        return OrderDto.builder()
                .orderId(order.getId())
                .shippingAddressResponseDto(ShippingAddressResponseDto.from(order))
                .paymentId(order.getPayment().getId())
                .status(order.getStatus())
                .build();
    }

    public static List<OrderDto> from(List<Order> orders) {
        return orders.stream()
                .map(OrderDto::from)
                .toList();
    }
}
