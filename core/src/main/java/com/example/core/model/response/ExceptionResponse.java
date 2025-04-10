package com.example.core.model.response;

import com.example.core.exception.BaseException;
import com.example.core.exception.ResponseErrorCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExceptionResponse {
    private ResponseErrorCode.ErrorStage stage;
    private int code;
    private String message;

    public static ExceptionResponse of(Exception exception) {
        if(exception instanceof BaseException) {
            return ExceptionResponse.builder()
                    .stage(((BaseException) exception).getStage())
                    .code(((BaseException) exception).getCode())
                    .message(exception.getMessage())
                    .build();
        }
        return ExceptionResponse.builder()
                .stage(ResponseErrorCode.ErrorStage.ERROR)
                .code(-1)
                .message(exception.getMessage())
                .build();
    }

    public static ExceptionResponse of(String message) {
        return ExceptionResponse.builder()
                .stage(ResponseErrorCode.ErrorStage.ERROR)
                .code(-1)
                .message(message)
                .build();
    }
}