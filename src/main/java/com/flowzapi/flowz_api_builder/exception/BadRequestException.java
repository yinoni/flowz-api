package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

    public BadRequestException(String message, HttpStatus status) {
        super(message, status);
    }
}
