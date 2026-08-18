package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.model.ai.AIGenerateEvent;
import com.flowzapi.flowz_api_builder.model.ai.GenerateFlowRequest;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.rabbitMQ.AIFlowPublisher;
import com.flowzapi.flowz_api_builder.service.SseLogsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {
    private final SseLogsService sseLogsService;
    private final AIFlowPublisher aiFlowPublisher;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("alive");
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<SseEmitter> publishAIAndStream(@PathVariable String projectId, @RequestParam String query, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(sseLogsService.publishAIAndStream(projectId, query, userDetails.getId()));
    }

}
