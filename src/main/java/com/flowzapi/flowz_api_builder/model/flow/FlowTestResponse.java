package com.flowzapi.flowz_api_builder.model.flow;

public class FlowTestResponse {
    private String status;
    private boolean testPassed;
    private String failedAssertion;

    public FlowTestResponse() {
    }

    public FlowTestResponse(String status, String failedAssertion, boolean testPassed) {
        this.status = status;
        this.failedAssertion = failedAssertion;
        this.testPassed = testPassed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isTestPassed() {
        return testPassed;
    }

    public void setTestPassed(boolean testPassed) {
        this.testPassed = testPassed;
    }

    public String getFailedAssertion() {
        return failedAssertion;
    }

    public void setFailedAssertion(String failedAssertion) {
        this.failedAssertion = failedAssertion;
    }
}
