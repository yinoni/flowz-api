package com.flowzapi.flowz_api_builder.model.flow;

import com.flowzapi.flowz_api_builder.model.Step;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class FlowDTO {
    private String id;
    private String flowName;
    private String projectId;
    private String globalURL;
    private Map<String, Object> globalVariables;
    private Map<String, String> globalHeaders;
    private Map<String, Object> globalAssertions;
    private Instant lastModified;




}
