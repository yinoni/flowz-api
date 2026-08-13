package com.flowzapi.flowz_api_builder.model.ai;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AIGenerateEvent {
    private String query;
    private String ownerId;
    private String projectId;
    private String requestId;
}
