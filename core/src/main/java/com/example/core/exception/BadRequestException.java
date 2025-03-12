package com.example.core.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadRequestException extends BaseException {

    public BadRequestException() {
        super(ResponseErrorCode.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(ResponseErrorCode code) {
        super(code);
    }
}
