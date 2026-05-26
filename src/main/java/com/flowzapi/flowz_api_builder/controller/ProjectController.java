package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.project.ProjectDTO;
import com.flowzapi.flowz_api_builder.model.project.ProjectInput;
import com.flowzapi.flowz_api_builder.model.project.ProjectUpdateInput;
import com.flowzapi.flowz_api_builder.service.FlowService;
import com.flowzapi.flowz_api_builder.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.flowzapi.flowz_api_builder.controller.FlowController.GLOBAL_USER;

@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;


    @GetMapping("")
    public ResponseEntity<?> getProjectById(@RequestParam String projectId){
        return ResponseEntity.ok(projectService.findById(projectId));
    }

    @PostMapping("")
    public ResponseEntity<?> createProject(@RequestBody ProjectInput projectInput) {
        ProjectDTO newProject = projectService.createProject(projectInput);

        return ResponseEntity.ok(newProject);
    }

    @PatchMapping("")
    public ResponseEntity<?> updateProject(@RequestBody ProjectUpdateInput projectInput) {
        return ResponseEntity.ok(projectService.updateProject(projectInput));
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteProject(@RequestParam String projectId) {
        projectService.deleteProject(projectId, GLOBAL_USER);
        return ResponseEntity.ok("project deleted");
    }

    @GetMapping("/user")
    public ResponseEntity<?> getProjectByUserId() {
        List<ProjectDTO> projectDTOList = projectService.findByUserId(GLOBAL_USER);
        return ResponseEntity.ok(projectDTOList);
    }
}
