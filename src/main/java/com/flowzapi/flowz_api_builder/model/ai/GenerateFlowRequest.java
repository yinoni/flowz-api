package com.flowzapi.flowz_api_builder.model.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GenerateFlowRequest {
    @NotBlank(message = "Query is required")
    private String query;

    @NotBlank(message = "Project id is required")
    private String projectId;
}
