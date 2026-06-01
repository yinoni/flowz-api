package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class InvalidVerificationException extends BaseException {
    public InvalidVerificationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
