package com.example.core.exception;

import com.example.core.model.response.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class APIExceptionControllerAdvice {

    // todo: create log
    private void createLog(HttpServletRequest request, Exception exception) {

    }

    @ExceptionHandler({BadRequestException.class, BadCredentialsException.class}) // 400
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse badRequest(HttpServletRequest request, Exception e) {
        return ExceptionResponse.of(e);
    }



}
