package com.flowzapi.flowz_api_builder.model.flow;

import com.flowzapi.flowz_api_builder.model.Step;

import java.util.List;

public class FlowDTO {
    private String id;
    private String flowName;
    private String projectId;


    public FlowDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
