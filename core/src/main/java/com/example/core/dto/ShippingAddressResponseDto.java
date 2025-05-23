package com.example.core.dto;

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
    private Long shippingAddressId;
    private String recipientName;
    private String address;
    private String zipCode;
    private String phoneNumber;
    private Boolean isDefault;

    public static ShippingAddressResponseDto from(ShippingAddress shippingAddress) {
        return ShippingAddressResponseDto.builder()
                .shippingAddressId(shippingAddress.getId())
                .recipientName(shippingAddress.getRecipientName())
                .address(shippingAddress.getAddress())
                .zipCode(shippingAddress.getZipCode())
                .phoneNumber(shippingAddress.getPhoneNumber())
                .isDefault(shippingAddress.getIsDefault())
                .build();
    }
}
