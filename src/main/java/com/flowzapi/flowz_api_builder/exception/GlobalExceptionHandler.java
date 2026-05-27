package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.LocalTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), e.getStatus(), LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, e.getStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
        ErrorResponse errorResponse = new ErrorResponse("Email or password incorrect!", HttpStatus.UNAUTHORIZED, LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }
}
