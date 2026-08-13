package com.flowzapi.flowz_api_builder.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowzapi.flowz_api_builder.model.enums.Method;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AIGenerateResultEvent {
    private int version;
    private String requestId;
    private String ownerId;
    private String projectId;
    private Status status;
    private ResultFlow flow;
    private String error;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class ResultFlow {
        private String flowName;
        private List<ResultSteps> steps;

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        @Setter
        public static class ResultSteps {
            private String title;
            private Method httpMethod;
            private String path;
            private String body;
            private Map<String, String> extract;
            private Map<String, String> assertions;
        }

    }

    public enum Status {
        success,  failed
    }
}
