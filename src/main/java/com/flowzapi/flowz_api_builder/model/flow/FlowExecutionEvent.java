package com.flowzapi.flowz_api_builder.model.flow;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
public class FlowExecutionEvent {
    private String userId;
    private String flowId;
}
