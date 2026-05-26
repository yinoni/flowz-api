package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.StepBuilder;
import com.flowzapi.flowz_api_builder.model.flow.FlowDTO;
import com.flowzapi.flowz_api_builder.model.flow.FlowInput;
import com.flowzapi.flowz_api_builder.model.flow.FlowTestResponse;
import com.flowzapi.flowz_api_builder.service.FlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.flowzapi.flowz_api_builder.model.StepBuilder.aStep;

@RestController
@RequestMapping("/flow")
public class FlowController {

    @Autowired
    private FlowService flowService;

    public static final String GLOBAL_USER = "6a154be400aa665ae2f8c4ce";

    @GetMapping("")
    public ResponseEntity<FlowDTO> getFlow(@RequestParam String flowId) {
        return ResponseEntity.ok(flowService.getFlow(flowId, GLOBAL_USER));
    }

    @PostMapping("")
    public ResponseEntity<FlowDTO> createFlow(@RequestBody FlowInput flowInput) {
        FlowDTO flowDTO = flowService.createFlow(flowInput);

        return ResponseEntity.ok(flowDTO);
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteFlow(@RequestParam String flowId) {
        flowService.deleteFlow(flowId, GLOBAL_USER);
        return ResponseEntity.ok("Flow deleted");
    }

    @GetMapping("/project")
    public ResponseEntity<List<FlowDTO>> getProjectFlows(@RequestParam String projectId) {
        List<FlowDTO> flowDTOS = flowService.getFlowsByProjectId(projectId, GLOBAL_USER);
        return ResponseEntity.ok(flowDTOS);
    }

    @GetMapping("/steps")
    public ResponseEntity<List<Step>> getFlowSteps(@RequestParam String flowId) {
        List<Step> steps = flowService.getFlowSteps(flowId, GLOBAL_USER);
        return ResponseEntity.ok(steps);
    }

    @DeleteMapping("/steps/{flowId}")
    public ResponseEntity<?> deleteFlowSteps(@RequestParam String stepId, @PathVariable String flowId) {
        flowService.deleteStep(flowId, stepId, GLOBAL_USER);

        return ResponseEntity.ok("step deleted");
    }

    @PostMapping("/steps/{flowId}")
    public ResponseEntity<?> createStep(@RequestBody Step step, @PathVariable String flowId) {
        flowService.addStep(flowId, step, GLOBAL_USER);

        return ResponseEntity.ok("step added");
    }

    @PostMapping("/execute/{flowId}")
    public ResponseEntity<FlowTestResponse> executeTemp(@PathVariable String flowId) {
        FlowTestResponse testResponse = flowService.executeSteps(flowId, GLOBAL_USER);
        if(testResponse.isTestPassed() && testResponse.getStatus().equals("COMPLETED"))
            return ResponseEntity.ok(testResponse);

        return new ResponseEntity<>(testResponse, HttpStatus.ACCEPTED);
    }

}
