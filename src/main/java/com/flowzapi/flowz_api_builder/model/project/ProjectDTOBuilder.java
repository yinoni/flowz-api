package com.flowzapi.flowz_api_builder.model.project;

public final class ProjectDTOBuilder {
    private String id;
    private String projectName;

    private ProjectDTOBuilder() {
    }

    public static ProjectDTOBuilder aProjectDTO() {
        return new ProjectDTOBuilder();
    }

    public ProjectDTOBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public ProjectDTOBuilder withProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public ProjectDTO build() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(id);
        projectDTO.setProjectName(projectName);
        return projectDTO;
    }
}
