package com.example.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatus {
    CREATED("주문 생성"),
    PENDING("결제 대기 중"),
    PAID("결제 완료, 상품 준비 중"),
    DELIVERING("모든 상품 배송 중"),
    DELIVERED("모든 상품 배송 완료"),
    CANCELLED("주문 취소");

    private final String description;
}
