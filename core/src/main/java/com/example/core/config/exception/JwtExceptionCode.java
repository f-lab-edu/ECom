package com.example.core.config.exception;

import lombok.Getter;

@Getter
public enum JwtExceptionCode {

    UNKNOWN_ERROR("UNKNOWN_ERROR"),
    NOT_FOUND_TOKEN("NOT_FOUND_TOKEN"),
    INVALID_TOKEN("INVALID_TOKEN"),
    EXPIRED_TOKEN("EXPIRED_TOKEN"),
    UNSUPPORTED_TOKEN("UNSUPPORTED_TOKEN"),
    WRONG_TOKEN_TYPE("WRONG_TYPE_TOKEN");

    private final String code;

    JwtExceptionCode(String code) {
        this.code = code;
    }

}