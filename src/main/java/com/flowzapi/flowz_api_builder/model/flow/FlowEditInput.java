package com.flowzapi.flowz_api_builder.model.flow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@Data
public class FlowEditInput {
    private static final String flowIdMessage = "Flow id required!";

    @NotBlank(message = flowIdMessage)
    private String id;

    @Size(min = 5, max = 20, message = "Flow name size should be between 5 - 20 characters")
    private String flowName;

    @NotBlank(message = "Global URL is required")
    private String globalURL;

    @NotNull(message = "Global headers is required")
    private Map<String, String> globalHeaders;

    @NotNull(message = "Global variables is required")
    private Map<String, Object> globalVariables;
}
