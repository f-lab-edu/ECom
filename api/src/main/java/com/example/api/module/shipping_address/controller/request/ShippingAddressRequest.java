package com.example.api.module.shipping_address.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingAddressRequest {

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(01[016789])-?([0-9]{3,4})-?([0-9]{4})$", message = "올바른 휴대폰 번호를 입력해 주세요.")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Zipcode is required")
    @Pattern(regexp = "^[0-9]{5}$", message = "올바른 우편번호(5자리 숫자)를 입력해 주세요.")
    private String zipCode;

    private boolean isDefault;
}