package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends BaseException{

    public AuthenticationException(String message, HttpStatus status) {
        super(message, status);
    }
}
