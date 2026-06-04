package com.flowzapi.flowz_api_builder.model;

import com.flowzapi.flowz_api_builder.model.flow.FlowDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

import static com.flowzapi.flowz_api_builder.model.flow.FlowDTOBuilder.aFlowDTO;

@Data
@NoArgsConstructor
@Document(collection = "flows")
public class Flow {
    @Id
    private String id;
    private String flowName;
    private String projectId;
    private String ownerId;
    private List<Step> steps = new ArrayList<>();
    private String globalURL;
    private Map<String, Object> globalVariables;
    private Map<String, String> globalHeaders;
    private Map<String, Object> globalAssertions = new HashMap<>();


    public FlowDTO convertToDTO() {
        return aFlowDTO()
                .withFlowName(this.flowName)
                .withId(this.id)
                .withProjectId(this.projectId)
                .withGlobalURL(this.globalURL)
                .withGlobalVariables(this.globalVariables)
                .withGlobalHeaders(this.globalHeaders)
                .withGlobalAssertions(this.globalAssertions)
                .build();
    }
}
