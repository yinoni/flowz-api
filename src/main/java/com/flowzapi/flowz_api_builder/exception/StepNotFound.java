package com.flowzapi.flowz_api_builder.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

public class StepNotFound extends BaseException {
    public StepNotFound() {
        super("Step is not exists", HttpStatus.NOT_FOUND);
    }
}
