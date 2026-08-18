package com.flowzapi.flowz_api_builder.model.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIEvent {
    private EventType eventType;

    public enum EventType{
        UPSERT,
        DELETE,
        DELETE_BY_PROJECT
    }
}
