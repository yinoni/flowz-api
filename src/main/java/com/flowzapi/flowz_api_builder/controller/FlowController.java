package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.StepBuilder;
import com.flowzapi.flowz_api_builder.model.flow.*;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.service.FlowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.flowzapi.flowz_api_builder.model.StepBuilder.aStep;

@RestController
@RequestMapping("/flow")
public class FlowController {

    @Autowired
    private FlowService flowService;

    @GetMapping("")
    public ResponseEntity<FlowDTO> getFlow(@RequestParam String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(flowService.getFlow(flowId, customUserDetails.getId()));
    }

    @PostMapping("")
    public ResponseEntity<FlowDTO> createFlow(@RequestBody FlowInput flowInput, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        FlowDTO flowDTO = flowService.createFlow(flowInput, customUserDetails.getId());

        return ResponseEntity.ok(flowDTO);
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteFlow(@RequestParam String flowId,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.deleteFlow(flowId, customUserDetails.getId());
        return ResponseEntity.ok("Flow deleted");
    }

    @PatchMapping("")
    public ResponseEntity<?> editFlow(@Valid @RequestBody FlowEditInput flowEditInput, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.editFlow(flowEditInput, customUserDetails.getId());
        return ResponseEntity.ok("Flow edited");
    }

    @PatchMapping("/{flowId}/globals")
    public ResponseEntity<?> setGlobals(@PathVariable String flowId, @Valid @RequestBody SetGlobalsRequest setGlobalsRequest, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.setGlobals(setGlobalsRequest, flowId, customUserDetails.getId());
        return ResponseEntity.ok("GlobalAssertions added");
    }

    @GetMapping("/project")
    public ResponseEntity<List<FlowDTO>> getProjectFlows(@RequestParam String projectId,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<FlowDTO> flowDTOS = flowService.getFlowsByProjectId(projectId, customUserDetails.getId());
        return ResponseEntity.ok(flowDTOS);
    }

    @GetMapping("/steps")
    public ResponseEntity<List<Step>> getFlowSteps(@RequestParam String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<Step> steps = flowService.getFlowSteps(flowId, customUserDetails.getId());
        return ResponseEntity.ok(steps);
    }

    @DeleteMapping("/steps/{flowId}")
    public ResponseEntity<?> deleteFlowStep(@RequestParam String stepId, @PathVariable String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.deleteStep(flowId, stepId, customUserDetails.getId());

        return ResponseEntity.ok("step deleted");
    }

    @PatchMapping("/steps/{flowId}")
    public ResponseEntity<?> editFlowStep(@RequestBody Step stepEditInput, @PathVariable String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.editStep(flowId, stepEditInput, customUserDetails.getId());

        return ResponseEntity.ok("step edited");
    }

    @PostMapping("/steps/{flowId}")
    public ResponseEntity<?> createStep(@RequestBody Step step, @PathVariable String flowId,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.addStep(flowId, step, customUserDetails.getId());

        return ResponseEntity.ok("step added");
    }

    @PatchMapping("/steps/{flowId}/reorder")
    public ResponseEntity<?> reorderSteps(@RequestBody ReorderStepsRequest stepsRequest, @PathVariable String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.reorderSteps(flowId, stepsRequest, customUserDetails.getId());

        return ResponseEntity.ok("Steps reordered added");
    }

}
