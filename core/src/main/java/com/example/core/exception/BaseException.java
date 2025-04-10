package com.example.core.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

    private final ResponseErrorCode.ErrorStage stage;
    private final int code;
    private final String message;

    public BaseException(ResponseErrorCode code) {
        this.code = code.getValue();
        this.message = code.getMessage();
        this.stage = code.getStage();
    }

    public BaseException(String message) {
        code = 0;
        this.message = message;
        stage = ResponseErrorCode.ErrorStage.ERROR;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public String getMessage() {
        return message;
    }
}
