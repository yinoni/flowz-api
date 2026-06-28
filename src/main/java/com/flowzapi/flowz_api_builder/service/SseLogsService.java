package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.rabbitMQ.FlowPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseLogsService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final FlowPublisherService flowPublisherService;
    private final FlowService flowService;


    public SseEmitter executeAndStream(String flowId, String userId) {
        String executionId = UUID.randomUUID().toString();

        flowService.findOwnerIdProjectedById(flowId, userId);

        SseEmitter emitter = createEmitter(executionId);

        try {
            emitter.send(SseEmitter.event().name("init").data(Map.of("executionId", executionId)));
        } catch (IOException e) {
            emitters.remove(executionId);
            return emitter;
        }

        try {
            flowPublisherService.publishFlowExecution(userId, flowId, executionId);
        } catch (Exception e) {
            log.error("Failed to publish flow execution for executionId {}: {}", executionId, e.getMessage());
            emitters.remove(executionId);
            try {
                emitter.send(SseEmitter.event().name("error").data(Map.of("message", "Failed to start execution, please try again")));
            } catch (IOException ignored) {}
        }

        return emitter;
    }

    public SseEmitter createEmitter(String executionId) {
        SseEmitter emitter = new SseEmitter(600_000L);
        emitters.put(executionId, emitter);

        emitter.onCompletion(() -> emitters.remove(executionId));
        emitter.onTimeout(() -> emitters.remove(executionId));
        emitter.onError((ex) -> emitters.remove(executionId));

        return emitter;
    }

    public SseEmitter getEmitter(String executionId) {
        return emitters.get(executionId);
    }

    public void sendMessage(String executionId, Map<String, Object> message) {
        SseEmitter emitter = getEmitter(executionId);

        if (emitter == null) {
            log.warn("SseEmitter not found or already closed for executionId: {}", executionId);
            return;
        }

        try {
            emitter.send(SseEmitter.event().name("logs").data(message));
        } catch (Exception e) {
            log.warn("SseEmitter.sendMessage failed for executionId {}: {}", executionId, e.getMessage());
            emitters.remove(executionId);
        }
    }

    public void completeSession(String executionId) {
        SseEmitter emitter = emitters.get(executionId); // או getEmitter(executionId) שלך
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.error("Error completing SSE session for execution: {}", executionId, e);
                emitters.remove(executionId);
            }
        }
    }
}
