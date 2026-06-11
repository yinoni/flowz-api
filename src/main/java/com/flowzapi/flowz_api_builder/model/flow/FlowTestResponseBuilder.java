package com.flowzapi.flowz_api_builder.model.flow;

public final class FlowTestResponseBuilder {
    private String status;
    private boolean testPassed;
    private String message;

    private FlowTestResponseBuilder() {
    }

    public static FlowTestResponseBuilder aFlowTestResponse() {
        return new FlowTestResponseBuilder();
    }

    public FlowTestResponseBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public FlowTestResponseBuilder withTestPassed(boolean testPassed) {
        this.testPassed = testPassed;
        return this;
    }

    public FlowTestResponseBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public FlowTestResponse build() {
        FlowTestResponse flowTestResponse = new FlowTestResponse();
        flowTestResponse.setStatus(status);
        flowTestResponse.setTestPassed(testPassed);
        flowTestResponse.setMessage(message);
        return flowTestResponse;
    }
}
