package com.example.core.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotFoundException extends BaseException{
    public NotFoundException() {
        super(ResponseErrorCode.NOT_FOUND);
    }

    public NotFoundException(ResponseErrorCode code) {
        super(code);
    }
}
