package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.BadRequestException;
import com.flowzapi.flowz_api_builder.exception.UserNotAllowedException;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.project.ProjectDTO;
import com.flowzapi.flowz_api_builder.model.project.ProjectInput;
import com.flowzapi.flowz_api_builder.model.project.ProjectUpdateInput;
import com.flowzapi.flowz_api_builder.repos.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.flowzapi.flowz_api_builder.model.ProjectBuilder.aProject;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public Project findById(String id) {
        Project project = projectRepository.findById(id).orElseThrow(
                () -> new BadRequestException("Project not exists", HttpStatus.BAD_REQUEST)
        );

        return project;
    }

    public List<ProjectDTO> findByUserId(String userId) {

        List<Project> projects = projectRepository.findByUserId(userId);
        List<ProjectDTO> projectDTOS = new ArrayList<>();

        for (Project project : projects) {
            if(!project.getUserId().equals(userId))
                throw new UserNotAllowedException("User not allowed", HttpStatus.FORBIDDEN);

            projectDTOS.add(project.convertToDTO());
        }

        return projectDTOS;
    }

    public ProjectDTO createProject(ProjectInput projectInput) {
        Project project = aProject()
                .withUserId("999")
                .withProjectName(projectInput.getProjectName()).build();

        return projectRepository.save(project).convertToDTO();
    }

    public ProjectDTO updateProject(ProjectUpdateInput projectInput) {
        if(projectInput.getProjectName() == null || projectInput.getProjectName().equals(""))
            throw new BadRequestException("projectName is required", HttpStatus.BAD_REQUEST);

        Project project = this.findById(projectInput.getProjectId());

        project.setProjectName(projectInput.getProjectName());
        return projectRepository.save(project).convertToDTO();
    }

    public void deleteProject(String projectId, String userId) {
        Project project = this.findById(projectId);
        if(!project.getUserId().equals(userId))
            throw new UserNotAllowedException("You are not allowed to delete this project", HttpStatus.FORBIDDEN);

        projectRepository.delete(project);
    }

}
