package com.example.core.exception;

import com.example.core.model.response.ExceptionResponse;
import com.example.core.utils.MessageUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class APIExceptionControllerAdvice {

    private MessageUtil messageUtil;

    // todo: create log
    private void createLog(HttpServletRequest request, Exception exception) {

    }

    @ExceptionHandler({BadRequestException.class, BadCredentialsException.class}) // 400
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse badRequest(HttpServletRequest request, Exception e) {
        return ExceptionResponse.of(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse methodArgumentNotValid(
            HttpServletRequest request,
            MethodArgumentNotValidException e
    ) {
        // 1. 모든 FieldError 가져오기
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        // 2. 필요한 방식으로 메시지를 구성
        //    예) 첫 번째 에러 메시지만 가져오기
        String message = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Validation error");

        // 3. ExceptionResponse.of(...)에 전달
        return ExceptionResponse.of(message);
    }

}
