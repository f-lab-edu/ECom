package com.example.core.enums;

public enum CouponStatus {
    AVAILABLE("사용 가능"),
    RESERVED("예약됨"),
    USED("사용됨");

    private final String description;

    CouponStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
