package com.example.core.dto;

import com.example.core.domain.order.Order;
import com.example.core.domain.shipping_address.ShippingAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingAddressResponseDto {
    private String recipientName;
    private String address;
    private String zipCode;
    private String phoneNumber;

    public static ShippingAddressResponseDto from(Order order) {
        return ShippingAddressResponseDto.builder()
                .recipientName(order.getRecipientName())
                .address(order.getAddress())
                .zipCode(order.getZipCode())
                .phoneNumber(order.getPhoneNumber())
                .build();
    }
}
