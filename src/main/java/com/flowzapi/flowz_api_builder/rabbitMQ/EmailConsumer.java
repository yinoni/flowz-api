package com.flowzapi.flowz_api_builder.rabbitMQ;

import com.flowzapi.flowz_api_builder.config.RabbitMQConfig;
import com.flowzapi.flowz_api_builder.model.EmailEventDTO;
import com.flowzapi.flowz_api_builder.model.flow.FlowExecutionEvent;
import com.flowzapi.flowz_api_builder.service.EmailService;
import com.flowzapi.flowz_api_builder.service.FlowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {
    private final EmailService emailService;

    @RabbitListener(
            queues = RabbitMQConfig.EMAILS_QUEUE,
            concurrency = "5-10",
            containerFactory = "rabbitContainerFactory")
    public void consumeEmailSending(EmailEventDTO event) throws Exception {
        log.info("Sending private code to {}. The private code is {}", event.getToEmail(), event.getVerificationCode());
        emailService.sendVerificationEmail(event.getToEmail(), event.getVerificationCode());
    }
}
