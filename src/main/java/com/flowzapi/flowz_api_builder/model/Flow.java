package com.flowzapi.flowz_api_builder.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Flow {
    @Id
    private String id;
    private String flowName;
    private String projectID;
    private List<Step> steps;

    public Flow() {
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

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }


}
