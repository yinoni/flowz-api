package com.flowzapi.flowz_api_builder.exception;

import org.springframework.http.HttpStatus;

public class RateLimiterException extends BaseException {
    public RateLimiterException() {
        super("You have exceeded your request limit. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
    }
}
