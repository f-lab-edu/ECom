package com.example.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ShippingCompany {
    CJ_LOGISTICS("CJ대한통운"),
    KOREA_POST("우체국택배");

    private final String description;
}
