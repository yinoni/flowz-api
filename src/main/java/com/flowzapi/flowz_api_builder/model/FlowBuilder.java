package com.flowzapi.flowz_api_builder.model;

import java.util.List;

public final class FlowBuilder {
    private String id;
    private String flowName;
    private String projectID;
    private List<Step> steps;

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

    public FlowBuilder withProjectID(String projectID) {
        this.projectID = projectID;
        return this;
    }

    public FlowBuilder withSteps(List<Step> steps) {
        this.steps = steps;
        return this;
    }

    public Flow build() {
        Flow flow = new Flow();
        flow.setId(id);
        flow.setFlowName(flowName);
        flow.setProjectID(projectID);
        flow.setSteps(steps);
        return flow;
    }
}
