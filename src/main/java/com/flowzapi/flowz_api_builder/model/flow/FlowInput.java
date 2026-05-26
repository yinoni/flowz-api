package com.flowzapi.flowz_api_builder.model.flow;

public class FlowInput {
    private String projectId;
    private String flowName;

    public FlowInput() {
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }
}
