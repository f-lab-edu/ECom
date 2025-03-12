package com.example.core.model.response;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS("0000", "정상 처리되었습니다."),
    ;

    private final String code;
    private final String message;

    ResponseCode(final String code, final String message) {
        this.code = code;
        this.message = message;
    }
}
