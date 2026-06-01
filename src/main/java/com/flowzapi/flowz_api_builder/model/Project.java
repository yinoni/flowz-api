package com.flowzapi.flowz_api_builder.model;

import com.flowzapi.flowz_api_builder.model.project.ProjectDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "projects")
public class Project {
    @Id
    private String id;

    @NotNull
    private String projectName;

    @NotNull
    private String userId;

    public Project() {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ProjectDTO convertToDTO() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(this.id);
        projectDTO.setProjectName(this.projectName);
        return projectDTO;
    }

}
