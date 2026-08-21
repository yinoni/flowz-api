package com.flowzapi.flowz_api_builder.rabbitMQ;

import com.flowzapi.flowz_api_builder.config.RabbitMQConfig;
import com.flowzapi.flowz_api_builder.model.ai.AIDeleteFlowByProjectIdEvent;
import com.flowzapi.flowz_api_builder.model.ai.AIDeleteFlowEvent;
import com.flowzapi.flowz_api_builder.model.ai.AIEvent;
import com.flowzapi.flowz_api_builder.model.ai.AIUpsertEvent;
import com.flowzapi.flowz_api_builder.model.flow.FlowIndexView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIFlowEventsPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAIUpsertEvent(FlowIndexView flowIndexView) {
        AIUpsertEvent aiUpsertEvent = new AIUpsertEvent(flowIndexView);
        send(aiUpsertEvent, flowIndexView.getFlowId());
    }

    public void publishVectorFlowDelete(String flowId){
        AIDeleteFlowEvent aiDeleteFlowEvent = new AIDeleteFlowEvent(flowId);
        send(aiDeleteFlowEvent, flowId);
    }

    public void publishVectorFlowDeleteByProjectId(String projectId){
        AIDeleteFlowByProjectIdEvent aiDeleteFlowEvent = new AIDeleteFlowByProjectIdEvent(projectId);
        send(aiDeleteFlowEvent, projectId);
    }

    public void send(AIEvent event, String context){
        try{
            rabbitTemplate.convertAndSend("", RabbitMQConfig.AI_FLOW_EVENTS, event);
        } catch(Exception e){
            log.error("Failed to publish {} for {}", event.getEventType(), context, e);
        }
    }
}
