package com.flowzapi.flowz_api_builder.model.step;

public final class StepWSResponseBuilder {
    private String stepId;
    private String message;
    private String status;
    private boolean success;

    private StepWSResponseBuilder() {
    }

    public static StepWSResponseBuilder aStepWSResponse() {
        return new StepWSResponseBuilder();
    }

    public StepWSResponseBuilder withStepId(String stepId) {
        this.stepId = stepId;
        return this;
    }

    public StepWSResponseBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public StepWSResponseBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public StepWSResponseBuilder withSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public StepWSResponse build() {
        StepWSResponse stepWSResponse = new StepWSResponse();
        stepWSResponse.setStepId(stepId);
        stepWSResponse.setMessage(message);
        stepWSResponse.setStatus(status);
        stepWSResponse.setSuccess(success);
        return stepWSResponse;
    }
}
