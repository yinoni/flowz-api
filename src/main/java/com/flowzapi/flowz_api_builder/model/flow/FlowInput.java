package com.flowzapi.flowz_api_builder.model.flow;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class FlowInput {
    private String projectId;
    private String flowName;
    private String globalURL;
    private Map<String, String> globalHeaders = new HashMap<>();
    private Map<String, Object> globalVariables = new HashMap<>();

}
