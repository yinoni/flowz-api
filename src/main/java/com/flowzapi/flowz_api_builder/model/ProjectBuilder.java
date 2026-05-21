package com.flowzapi.flowz_api_builder.model;

public final class ProjectBuilder {
    private String id;
    private String projectName;
    private String userId;

    private ProjectBuilder() {
    }

    public static ProjectBuilder aProject() {
        return new ProjectBuilder();
    }

    public ProjectBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public ProjectBuilder withProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public ProjectBuilder withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Project build() {
        Project project = new Project();
        project.setId(id);
        project.setProjectName(projectName);
        project.setUserId(userId);
        return project;
    }
}
