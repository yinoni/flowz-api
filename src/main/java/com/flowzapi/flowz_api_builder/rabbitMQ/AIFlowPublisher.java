package com.flowzapi.flowz_api_builder.rabbitMQ;


import com.flowzapi.flowz_api_builder.config.RabbitMQConfig;
import com.flowzapi.flowz_api_builder.model.ai.AIGenerateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIFlowPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishAIGenerateEvent(AIGenerateEvent aiGenerateEvent) {
        try{
            rabbitTemplate.convertAndSend("", RabbitMQConfig.AI_GENERATE_QUEUE, aiGenerateEvent);
            log.info("Started the AI flow generation for user {} --> Request ID: {}", aiGenerateEvent.getRequestId(), aiGenerateEvent.getOwnerId());
        } catch (Exception e) {
            log.error("Failed to publish AI generate event for {}", aiGenerateEvent.getRequestId(), e);
        }
    }
}
