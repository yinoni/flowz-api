package com.flowzapi.flowz_api_builder.model.flow;

public final class FlowDTOBuilder {
    private String id;
    private String flowName;
    private String projectId;

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

    public FlowDTO build() {
        FlowDTO flowDTO = new FlowDTO();
        flowDTO.setId(id);
        flowDTO.setFlowName(flowName);
        flowDTO.setProjectId(projectId);
        return flowDTO;
    }
}
