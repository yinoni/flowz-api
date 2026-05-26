package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class UserNotAllowedException extends BaseException {

    public UserNotAllowedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
