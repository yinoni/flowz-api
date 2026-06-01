package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.StepBuilder;
import com.flowzapi.flowz_api_builder.model.flow.FlowDTO;
import com.flowzapi.flowz_api_builder.model.flow.FlowEditInput;
import com.flowzapi.flowz_api_builder.model.flow.FlowInput;
import com.flowzapi.flowz_api_builder.model.flow.FlowTestResponse;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.service.FlowService;
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
    public ResponseEntity<?> editFlow(@RequestBody FlowEditInput flowEditInput, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.editFlow(flowEditInput, customUserDetails.getId());
        return ResponseEntity.ok("Flow edited");
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

    @PutMapping("/steps/{flowId}")
    public ResponseEntity<?> editFlowStep(@RequestBody Step stepEditInput, @PathVariable String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.editStep(flowId, stepEditInput, customUserDetails.getId());

        return ResponseEntity.ok("step edited");
    }

    @PostMapping("/steps/{flowId}")
    public ResponseEntity<?> createStep(@RequestBody Step step, @PathVariable String flowId,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        flowService.addStep(flowId, step, customUserDetails.getId());

        return ResponseEntity.ok("step added");
    }

    /*
    @PostMapping("/execute/{flowId}")
    public ResponseEntity<FlowTestResponse> executeTemp(@PathVariable String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        FlowTestResponse testResponse = flowService.executeSteps(flowId, customUserDetails.getId());
        if(testResponse.isTestPassed() && testResponse.getStatus().equals("COMPLETED"))
            return ResponseEntity.ok(testResponse);

        return new ResponseEntity<>(testResponse, HttpStatus.ACCEPTED);
    }

     */

}
