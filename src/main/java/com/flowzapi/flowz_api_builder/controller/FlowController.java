package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.flow.*;
import com.flowzapi.flowz_api_builder.model.step.StepRequest;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.service.FlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flow")
@RequiredArgsConstructor
public class FlowController {


    private final FlowService flowService;

    @GetMapping("/{flowId}")
    public ResponseEntity<FlowDTO> getFlow(@PathVariable String flowId, @RequestParam String projectId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(flowService.findById(projectId, flowId, customUserDetails.getId()));
    }

    @PostMapping("")
    public ResponseEntity<FlowDTO> createFlow(@RequestBody FlowInput flowInput, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        FlowDTO flowDTO = flowService.createFlow(flowInput, customUserDetails.getId());

        return ResponseEntity.ok(flowDTO);
    }

    @DeleteMapping("/{flowId}")
    public ResponseEntity<?> deleteFlow(@PathVariable String flowId,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
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
    public ResponseEntity<FlowStepsResponse> getFlowSteps(@RequestParam String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        FlowStepsResponse flowSteps = flowService.getFlowSteps(flowId, customUserDetails.getId());
        return ResponseEntity.ok(flowSteps);
    }

    @DeleteMapping("/steps/{flowId}")
    public ResponseEntity<?> deleteFlowStep(@RequestParam String stepId, @PathVariable String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.deleteStep(flowId, stepId, customUserDetails.getId());

        return ResponseEntity.ok("step deleted");
    }

    @PatchMapping("/steps/{flowId}")
    public ResponseEntity<?> editFlowStep(@RequestBody StepRequest stepRequest, @PathVariable String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.editStep(flowId, stepRequest, customUserDetails.getId());

        return ResponseEntity.ok("step edited");
    }

    @PostMapping("/steps/{flowId}")
    public ResponseEntity<?> createStep(@RequestBody StepRequest stepRequest, @PathVariable String flowId,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String stepId = flowService.addStep(flowId, stepRequest, customUserDetails.getId());

        return ResponseEntity.ok(stepId);
    }

    @PatchMapping("/steps/{flowId}/sync")
    public ResponseEntity<?> syncSteps(@RequestBody SyncStepsRequest stepsRequest, @PathVariable String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.syncCanvasSteps(flowId, customUserDetails.getId(), stepsRequest);

        return ResponseEntity.ok("Steps reordered added");
    }

    @DeleteMapping("/fallback/{flowId}")
    public ResponseEntity<?> deleteFlowFallback(@RequestParam String fallbackId, @PathVariable String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.deleteFallback(flowId, fallbackId, customUserDetails.getId());

        return ResponseEntity.ok("step deleted");
    }

    @PostMapping("/mockup/{projectId}")
    public ResponseEntity<Flow> createMockUpFlow(@PathVariable String projectId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(flowService.createMockupFlow(projectId, customUserDetails.getId()));
    }
}
