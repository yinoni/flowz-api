package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class URLNotValidException extends BaseException {
    public URLNotValidException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
