package com.flowzapi.flowz_api_builder.model.step;

import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.service.FlowService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

import static com.flowzapi.flowz_api_builder.model.StepBuilder.aStep;

@Getter
@Setter
@NoArgsConstructor
public class StepRequest {
    private Step step;
    private FlowService.StepGroup stepGroup;
}
