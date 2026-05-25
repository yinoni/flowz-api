package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {
    private HttpStatus status;
    public BaseException(String message, HttpStatus status) {
        super(message);
        this.status=status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
