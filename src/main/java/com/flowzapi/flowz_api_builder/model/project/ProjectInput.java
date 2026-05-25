package com.flowzapi.flowz_api_builder.model.project;

public class ProjectInput {
    private String projectName;

    public ProjectInput(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
