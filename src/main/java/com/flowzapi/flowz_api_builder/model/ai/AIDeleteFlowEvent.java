package com.flowzapi.flowz_api_builder.model.ai;

import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
public class AIDeleteFlowEvent extends AIEvent{
    private String flowId;

    public AIDeleteFlowEvent(String flowId){
        super(EventType.DELETE);
        this.flowId = flowId;
    }

}
