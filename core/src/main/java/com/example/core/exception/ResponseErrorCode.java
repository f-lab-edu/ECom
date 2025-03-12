package com.example.core.exception;

import lombok.Getter;

import java.io.Serializable;

@Getter
public enum ResponseErrorCode implements Serializable {

    /// Common response code
    BAD_REQUEST(400, "잘못된 요청입니다.", ErrorStage.WARN),
    UN_AUTHORIZED(401, "인증 권한이 없습니다.", ErrorStage.WARN),
    PERMISSION_DENIED(403, "접근권한이 없습니다.", ErrorStage.WARN),
    NOT_FOUND(404, "요청하신 정보를 찾을 수 없습니다.", ErrorStage.WARN),

    // 400
    TOKEN_EXPIRED(40100, "토큰이 만료되었습니다.", ErrorStage.INFO),
    TOKEN_REQUIRED(40101, "토큰이 필요합니다.", ErrorStage.WARN),
    TOKEN_INVALID(40102, "유효하지 않은 토큰입니다.", ErrorStage.WARN),

    ;
    public enum ErrorStage { INFO, WARN, ERROR }

    final private int value;
    final private String message;
    final private ErrorStage stage;

    ResponseErrorCode(final int value, final String message, final ErrorStage stage) {
        this.value = value;
        this.message = message;
        this.stage = stage;
    }
}