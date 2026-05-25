package com.flowzapi.flowz_api_builder.model.project;

public class ProjectDTO {
    private String projectId;
    private String projectName;

    public ProjectDTO() {
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
