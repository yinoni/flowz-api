package com.flowzapi.flowz_api_builder.rabbitMQ;

import com.flowzapi.flowz_api_builder.config.RabbitMQConfig;
import com.flowzapi.flowz_api_builder.model.flow.FlowExecutionEvent;
import com.flowzapi.flowz_api_builder.service.FlowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlowExecutionConsumer {
    private final FlowExecutionService flowExecutionService;

    @RabbitListener(
            queues = RabbitMQConfig.QUEUE_FLOWS,
            concurrency = "3-5",
            containerFactory = "rabbitContainerFactory")
    public void consumeFlowExecution(FlowExecutionEvent event){
        log.info("Worker picked up execution id {} for user {} from RabbitMQ", event.getExecutionId(), event.getUserId());
        flowExecutionService.executeFlow(event.getExecutionId(), event.getFlowId(), event.getUserId());
    }
}
