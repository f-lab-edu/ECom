package com.example.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ShippingStatus {
    READY("배송 준비 중"),
    DELIVERING("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("배송 취소"),
    FAILED("배송 실패");

    private final String description;
}
