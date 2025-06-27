package com.example.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentMethod {
    CARD("CARD"),
    KAKAOPAY("KAKAOPAY");

    private final String method;

    public String getMethod() {
        return method;
    }
}
