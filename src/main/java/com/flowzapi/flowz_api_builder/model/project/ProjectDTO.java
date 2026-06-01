package com.flowzapi.flowz_api_builder.model.project;

public class ProjectDTO {
    private String id;
    private String projectName;

    public ProjectDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
