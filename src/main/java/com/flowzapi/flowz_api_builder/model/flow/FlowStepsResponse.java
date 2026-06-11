package com.flowzapi.flowz_api_builder.model.flow;

import com.flowzapi.flowz_api_builder.model.Step;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor

public class FlowStepsResponse {
    private List<Step> steps;
    private List<Step> fallbacks;

    public FlowStepsResponse(List<Step> steps, List<Step> fallbacks) {
        this.steps = steps;
        this.fallbacks = fallbacks;
    }
}
