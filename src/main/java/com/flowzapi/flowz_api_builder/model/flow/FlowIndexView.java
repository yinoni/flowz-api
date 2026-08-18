package com.flowzapi.flowz_api_builder.model.flow;

import com.flowzapi.flowz_api_builder.model.enums.Method;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FlowIndexView {
    private String flowId;
    private String flowName;
    private String ownerId;
    private String projectId;
    private Instant flowLastModified;
    private List<StepIndexView> steps;
    private Long version;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class StepIndexView{
        private String title;
        private String url;
        private Method httpMethod;
        private Map<String, Object> assertions;
        private Map<String, String> extract;
    }
}
