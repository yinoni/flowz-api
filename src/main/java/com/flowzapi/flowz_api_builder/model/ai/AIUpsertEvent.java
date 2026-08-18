package com.flowzapi.flowz_api_builder.model.ai;

import com.flowzapi.flowz_api_builder.model.flow.FlowIndexView;
import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
public class AIUpsertEvent extends AIEvent{
    private FlowIndexView flowIndexView;

    public AIUpsertEvent(FlowIndexView flowIndexView){
        super(EventType.UPSERT);
        this.flowIndexView = flowIndexView;
    }
}
