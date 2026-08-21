package com.flowzapi.flowz_api_builder.rabbitMQ;

import com.flowzapi.flowz_api_builder.config.RabbitMQConfig;
import com.flowzapi.flowz_api_builder.model.flow.FlowExecutionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlowPublisherService {
    private final RabbitTemplate rabbitTemplate;

    public void publishFlowExecution(String userId, String flowId, String executionId) {
        FlowExecutionEvent event = new FlowExecutionEvent(userId, flowId, executionId);

        try{
            rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_FLOWS, event);
            log.info("Execution id {} published to RabbitMQ for user {}", executionId, userId);
        }
        catch (Exception e){
            log.error("Failed to publish flow generation event for flow: {}, execution id: {}", flowId, executionId, e);
        }

    }
}
