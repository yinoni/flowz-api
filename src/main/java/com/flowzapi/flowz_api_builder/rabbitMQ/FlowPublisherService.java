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

    public void publishFlowExecution(String userId, String flowId) {
        FlowExecutionEvent event = new FlowExecutionEvent(userId, flowId);

        log.info("Pushing Flow {} to RabbitMQ for user {}", flowId, userId);

        rabbitTemplate.convertAndSend(RabbitMQConfig.FLOWS_EXCHANGE, RabbitMQConfig.ROUTING_KEY_FLOWS, event);
    }
}
