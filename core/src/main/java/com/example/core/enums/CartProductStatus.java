package com.example.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CartProductStatus {
    AVAILABLE("available", "장바구니에 담긴 상품"),
    SHORTAGE("shortage", "재고가 부족한 상품"),
    DELETED("deleted", "삭제된 상품");

    private String code;
    private String message;
}
