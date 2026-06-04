package com.flowzapi.flowz_api_builder.model.step;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class StepWSResponse {
    private String stepId;
    private String message;
    private String status;
    private boolean success;
}
