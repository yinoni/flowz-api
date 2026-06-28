package com.flowzapi.flowz_api_builder.model.step;

import com.flowzapi.flowz_api_builder.model.flow.WSMessage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class StepWSResponse extends WSMessage {
    private String stepId;
    private String response;
}
