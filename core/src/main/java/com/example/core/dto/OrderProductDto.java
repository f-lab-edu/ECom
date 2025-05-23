package com.example.core.dto;

import com.example.core.domain.order_product.OrderProduct;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderProductDto {
    private Long orderProductId;
    private Long orderId;
    private Long productId;
    private Long quantity;
    private Long price;

    public static OrderProductDto from(OrderProduct orderProduct) {
        return OrderProductDto.builder()
                .orderProductId(orderProduct.getId())
                .orderId(orderProduct.getOrder().getId())
                .productId(orderProduct.getProduct().getId())
                .quantity(orderProduct.getQuantity())
                .price(orderProduct.getProductPrice())
                .build();
    }

    public static List<OrderProductDto> from(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .map(OrderProductDto::from)
                .toList();
    }
}
