package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class StepExecutionException extends BaseException {
    public StepExecutionException(String message, HttpStatus status) {
        super(message, status);
    }
}
