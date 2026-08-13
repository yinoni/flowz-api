package com.flowzapi.flowz_api_builder.rabbitMQ;

import com.flowzapi.flowz_api_builder.config.RabbitMQConfig;
import com.flowzapi.flowz_api_builder.model.EmailEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j

public class AIResultConsumer {

    @RabbitListener(
            queues = RabbitMQConfig.AI_RESULT_QUEUE,
            concurrency = "1",
            containerFactory = "rabbitContainerFactory")
    public void consumeAIResult(EmailEventDTO event) {
        log.info("Sending private code to {}. The private code is {}", event.getToEmail(), event.getVerificationCode());
    }
}
