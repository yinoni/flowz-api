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
public class ReorderStepsRequest {
    private List<ReorderItem> steps;


    @Data
    @Getter
    @Setter
    @NoArgsConstructor
   public static class ReorderItem{
        private String id;
        private Step.Position position;
    }
}
