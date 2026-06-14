package com.flowzapi.flowz_api_builder.model.flow;

import com.flowzapi.flowz_api_builder.model.Step;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class SyncStepsRequest {
    private Step step;
    private List<ReorderItem> reorderSteps;

    @Data
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReorderItem{
        private String id;
        private Step.Position position;
    }
}

