package com.flowzapi.flowz_api_builder.model.flow;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.flowzapi.flowz_api_builder.model.step.StepWSResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StepWSResponse.class, name = "STEP"),
        @JsonSubTypes.Type(value = FlowWSMessage.class, name = "FLOW")
})
public abstract class WSMessage {
    private String status;
    private boolean success;
    private String message;
}
