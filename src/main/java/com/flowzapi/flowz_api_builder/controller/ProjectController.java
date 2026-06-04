package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.project.ProjectDTO;
import com.flowzapi.flowz_api_builder.model.project.ProjectInput;
import com.flowzapi.flowz_api_builder.model.project.ProjectUpdateInput;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.service.FlowService;
import com.flowzapi.flowz_api_builder.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;


    @GetMapping("")
    public ResponseEntity<?> getProjectById(@RequestParam String projectId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        System.out.println("CustomUserDetails: " + customUserDetails.getId());
        return ResponseEntity.ok(projectService.findById(projectId, customUserDetails.getId()));
    }

    @PostMapping("")
    public ResponseEntity<?> createProject(@Valid @RequestBody ProjectInput projectInput, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        ProjectDTO newProject = projectService.createProject(projectInput, customUserDetails.getId());

        return ResponseEntity.ok(newProject);
    }

    @PatchMapping("")
    public ResponseEntity<?> updateProject(@Valid @RequestBody ProjectUpdateInput projectInput, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(projectService.updateProject(projectInput, customUserDetails.getId()));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        projectService.deleteProject(projectId, customUserDetails.getId());
        return ResponseEntity.ok("project deleted");
    }

    @GetMapping("/user")
    public ResponseEntity<?> getProjectByUserId(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<ProjectDTO> projectDTOList = projectService.findByUserId(customUserDetails.getId());
        return ResponseEntity.ok(projectDTOList);
    }
}
