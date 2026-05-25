package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.ResponseEntity;
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
}
