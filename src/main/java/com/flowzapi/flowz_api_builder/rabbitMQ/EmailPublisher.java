package com.flowzapi.flowz_api_builder.rabbitMQ;

import com.flowzapi.flowz_api_builder.config.RabbitMQConfig;
import com.flowzapi.flowz_api_builder.model.EmailEventDTO;
import com.flowzapi.flowz_api_builder.service.FlowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void sendVerificationCodePublisher(String toEmail, String verificationCode){
        EmailEventDTO emailEventDTO = new EmailEventDTO(toEmail, verificationCode);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAILS_EXCHANGE, RabbitMQConfig.EMAILS_ROUTING_KEY, emailEventDTO);
    }
}
