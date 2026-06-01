package com.flowzapi.flowz_api_builder.model.flow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class FlowEditInput {
    private String id;
    private String flowName;
    private String globalURL;
    private Map<String, String> globalHeaders;
    private Map<String, Object> globalVariables;
}
