package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class SyncException extends BaseException {
    public SyncException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
