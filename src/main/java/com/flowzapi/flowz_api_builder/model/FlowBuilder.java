package com.flowzapi.flowz_api_builder.model;

import java.util.List;
import java.util.Map;

public final class FlowBuilder {
    private String id;
    private String flowName;
    private String projectId;
    private String ownerId;
    private List<Step> steps;
    private String globalURL;
    private Map<String, Object> globalVariables;
    private Map<String, String> globalHeaders;
    private Map<String, Object> globalAssertions;

    private FlowBuilder() {
    }

    public static FlowBuilder aFlow() {
        return new FlowBuilder();
    }

    public FlowBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public FlowBuilder withFlowName(String flowName) {
        this.flowName = flowName;
        return this;
    }

    public FlowBuilder withProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public FlowBuilder withOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public FlowBuilder withSteps(List<Step> steps) {
        this.steps = steps;
        return this;
    }

    public FlowBuilder withGlobalURL(String globalURL) {
        this.globalURL = globalURL;
        return this;
    }

    public FlowBuilder withGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
        return this;
    }

    public FlowBuilder withGlobalHeaders(Map<String, String> globalHeaders) {
        this.globalHeaders = globalHeaders;
        return this;
    }

    public FlowBuilder withGlobalAssertions(Map<String, Object> globalAssertions) {
        this.globalAssertions = globalAssertions;
        return this;
    }

    public Flow build() {
        Flow flow = new Flow();
        flow.setId(id);
        flow.setFlowName(flowName);
        flow.setProjectId(projectId);
        flow.setOwnerId(ownerId);
        flow.setSteps(steps);
        flow.setGlobalURL(globalURL);
        flow.setGlobalVariables(globalVariables);
        flow.setGlobalHeaders(globalHeaders);
        flow.setGlobalAssertions(globalAssertions);
        return flow;
    }
}
