package com.flowzapi.flowz_api_builder.model.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectUpdateInput {
    @NotBlank(message = "Project name is required!")
    @Size(min = 5, max = 20, message = "Project name size should be between 5-20 characters")
    private String projectName;

    @NotBlank(message = "Project ID is required!")
    private String projectId;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
