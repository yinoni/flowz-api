package com.flowzapi.flowz_api_builder.model.ai;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
public class AIDeleteFlowByProjectIdEvent extends AIEvent{
    private String projectId;

    public AIDeleteFlowByProjectIdEvent(String projectId) {
        super(EventType.DELETE_BY_PROJECT);
        this.projectId = projectId;
    }
}
