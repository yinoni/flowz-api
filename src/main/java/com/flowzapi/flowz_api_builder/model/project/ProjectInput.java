package com.flowzapi.flowz_api_builder.model.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectInput {
    @NotBlank(message = "Project name is required!")
    @Size(min = 5, max = 20, message = "Project name size should be between 5-20 characters")
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
