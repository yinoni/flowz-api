package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.service.FlowExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/execute")
@RequiredArgsConstructor
public class FlowExecutionController {

    private final FlowExecutionService flowExecutionService;

    @GetMapping("")
    public ResponseEntity<String> getExecutionID(@RequestParam String flowId, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        return ResponseEntity.ok(flowExecutionService.getExecutionID(flowId, customUserDetails.getId()));
    }

    @PostMapping("/{executionId}")
    public ResponseEntity<?> executeFlow(@PathVariable String executionId, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        flowExecutionService.executeFlow(executionId, customUserDetails.getId());

        return ResponseEntity.accepted().body("Executing the flow...");
    }

}
