package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class ProjectNotExistsException extends BaseException {
    public ProjectNotExistsException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
