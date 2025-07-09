package com.example.api.module.shipping_address.controller.response;

import com.example.core.domain.shipping_address.ShippingAddress;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShippingAddressResponse {

    private Long id;
    private String recipientName;
    private String phoneNumber;
    private String address;
    private String zipCode;
    private boolean isDefault;

    public static ShippingAddressResponse from(ShippingAddress shippingAddress) {
        return ShippingAddressResponse.builder()
                .id(shippingAddress.getId())
                .recipientName(shippingAddress.getRecipientName())
                .phoneNumber(shippingAddress.getPhoneNumber())
                .address(shippingAddress.getAddress())
                .zipCode(shippingAddress.getZipCode())
                .isDefault(shippingAddress.getIsDefault())
                .build();
    }

    // Getters and Setters
}
