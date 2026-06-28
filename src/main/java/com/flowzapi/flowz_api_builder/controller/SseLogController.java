package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.rabbitMQ.FlowPublisherService;
import com.flowzapi.flowz_api_builder.service.FlowService;
import com.flowzapi.flowz_api_builder.service.SseLogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class SseLogController {
    private final SseLogsService sseLogsService;

    @GetMapping(value = "/flows/{flowId}/execute", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> executeAndStream(@PathVariable String flowId, @AuthenticationPrincipal CustomUserDetails user) {
        SseEmitter emitter = sseLogsService.executeAndStream(flowId, user.getId());

      return ResponseEntity.ok(emitter);
    }
}
