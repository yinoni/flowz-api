package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class FlowNotFound extends BaseException {
    public FlowNotFound(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
