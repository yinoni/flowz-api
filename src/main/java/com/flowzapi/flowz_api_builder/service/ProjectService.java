package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.BadRequestException;
import com.flowzapi.flowz_api_builder.exception.ProjectNotExistsException;
import com.flowzapi.flowz_api_builder.exception.UserNotAllowedException;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.project.ProjectDTO;
import com.flowzapi.flowz_api_builder.model.project.ProjectInput;
import com.flowzapi.flowz_api_builder.model.project.ProjectUpdateInput;
import com.flowzapi.flowz_api_builder.repos.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.flowzapi.flowz_api_builder.model.ProjectBuilder.aProject;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Lazy
    @Autowired
    private FlowService flowService;

    public Project findById(String id, String userId) {
        Project project = projectRepository.findById(id).orElseThrow(
                () -> new ProjectNotExistsException("Project not exists")
        );

        if(!project.getUserId().equals(userId))
            throw new UserNotAllowedException("User not allowed to update project");

        return project;
    }

    public List<ProjectDTO> findByUserId(String userId) {

        List<Project> projects = projectRepository.findByUserId(userId);
        List<ProjectDTO> projectDTOS = new ArrayList<>();

        for (Project project : projects) {
            if(!project.getUserId().equals(userId))
                throw new UserNotAllowedException("User not allowed");

            projectDTOS.add(project.convertToDTO());
        }

        return projectDTOS;
    }

    public ProjectDTO createProject(ProjectInput projectInput, String userId) {
        Project project = aProject()
                .withUserId(userId)
                .withProjectName(projectInput.getProjectName()).build();

        return projectRepository.save(project).convertToDTO();
    }

    public ProjectDTO updateProject(ProjectUpdateInput projectInput, String userId) {
        if(projectInput.getProjectName() == null || projectInput.getProjectName().equals(""))
            throw new BadRequestException("projectName is required", HttpStatus.BAD_REQUEST);

        Project project = this.findById(projectInput.getProjectId(), userId);

        project.setProjectName(projectInput.getProjectName());
        return projectRepository.save(project).convertToDTO();
    }

    public void deleteProject(String projectId, String userId) {
        Project project = this.findById(projectId,  userId);
        if(!project.getUserId().equals(userId))
            throw new UserNotAllowedException("You are not allowed to delete this project");

        flowService.deleteFlowByProjectId(projectId);

        projectRepository.delete(project);
    }

    public List<ProjectDTO> getProjectsByUserId(String userId) {
        List<Project> projects = projectRepository.findByUserId(userId);

        return projects.stream().map(Project::convertToDTO).toList();
    }

}
