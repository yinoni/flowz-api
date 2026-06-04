package com.flowzapi.flowz_api_builder.model.flow;

import java.util.Map;

public final class FlowDTOBuilder {
    private String id;
    private String flowName;
    private String projectId;
    private String globalURL;
    private Map<String, Object> globalVariables;
    private Map<String, String> globalHeaders;
    private Map<String, Object> globalAssertions;

    private FlowDTOBuilder() {
    }

    public static FlowDTOBuilder aFlowDTO() {
        return new FlowDTOBuilder();
    }

    public FlowDTOBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public FlowDTOBuilder withFlowName(String flowName) {
        this.flowName = flowName;
        return this;
    }

    public FlowDTOBuilder withProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public FlowDTOBuilder withGlobalURL(String globalURL) {
        this.globalURL = globalURL;
        return this;
    }

    public FlowDTOBuilder withGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
        return this;
    }

    public FlowDTOBuilder withGlobalHeaders(Map<String, String> globalHeaders) {
        this.globalHeaders = globalHeaders;
        return this;
    }

    public FlowDTOBuilder withGlobalAssertions(Map<String, Object> globalAssertions) {
        this.globalAssertions = globalAssertions;
        return this;
    }

    public FlowDTO build() {
        FlowDTO flowDTO = new FlowDTO();
        flowDTO.setId(id);
        flowDTO.setFlowName(flowName);
        flowDTO.setProjectId(projectId);
        flowDTO.setGlobalURL(globalURL);
        flowDTO.setGlobalVariables(globalVariables);
        flowDTO.setGlobalHeaders(globalHeaders);
        flowDTO.setGlobalAssertions(globalAssertions);
        return flowDTO;
    }
}
