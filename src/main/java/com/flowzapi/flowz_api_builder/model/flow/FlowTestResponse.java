package com.flowzapi.flowz_api_builder.model.flow;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowTestResponse {
    private String status;
    private boolean testPassed;
    private String message;

    public FlowTestResponse() {
    }

    public FlowTestResponse(String status, String message, boolean testPassed) {
        this.status = status;
        this.message = message;
        this.testPassed = testPassed;
    }
}
