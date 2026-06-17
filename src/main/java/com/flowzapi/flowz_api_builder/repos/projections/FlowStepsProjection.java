package com.flowzapi.flowz_api_builder.repos.projections;

import com.flowzapi.flowz_api_builder.model.Step;

import java.util.List;

public interface FlowStepsProjection {
    String getOwnerId();
    List<Step> getSteps();
    List<Step> getFallbacks();
    String getId();
    String getProjectId();
}
