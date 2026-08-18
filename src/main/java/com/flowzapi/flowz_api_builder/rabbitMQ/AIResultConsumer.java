package com.flowzapi.flowz_api_builder.rabbitMQ;

import com.flowzapi.flowz_api_builder.config.RabbitMQConfig;
import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.ai.AIGenerateResultEvent;
import com.flowzapi.flowz_api_builder.model.flow.FlowDTO;
import com.flowzapi.flowz_api_builder.service.FlowService;
import com.flowzapi.flowz_api_builder.service.SseLogsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIResultConsumer {
    private final FlowService flowService;
    private final SseLogsService sseLogsService;

    @RabbitListener(
            queues = RabbitMQConfig.AI_RESULT_QUEUE,
            concurrency = "1",
            containerFactory = "rabbitContainerFactory")
    public void consumeAIResult(AIGenerateResultEvent event) {
        try{
        Flow flow = flowService.convertAIResultToFlow(event);
        FlowDTO flowDTO = flowService.saveFlow(flow);
            sseLogsService.sendMessage("complete", event.getRequestId(), Map.of("message", "Flow created successfully!", "flow", flowDTO));
        }
        catch (Exception e){
            sseLogsService.sendMessage("error", event.getRequestId(), Map.of("message", e.getMessage()));
        }
        finally {
            sseLogsService.completeSession(event.getRequestId());
        }
    }
}
