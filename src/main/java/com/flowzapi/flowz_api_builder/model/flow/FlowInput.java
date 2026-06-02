package com.flowzapi.flowz_api_builder.model.flow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class FlowInput {
    private static final String projectIdMessage = "Project id is required";
    private static final String flowNameMessage = "Flow name is required";

    @NotBlank(message = projectIdMessage)
    private String projectId;

    @NotBlank(message = flowNameMessage)
    @Size(min = 5, max = 20, message = "Flow name size should be between 5 - 20 characters")
    private String flowName;

    private String globalURL;
    private Map<String, String> globalHeaders = new HashMap<>();
    private Map<String, Object> globalVariables = new HashMap<>();

}
