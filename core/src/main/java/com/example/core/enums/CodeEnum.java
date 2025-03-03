package com.example.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeEnum {
    SUCCESS("0000", "SUCCESS"),
    UNKNOWN_ERROR("9999", "UNKNOWN ERROR");

    private String code;
    private String message;
}
