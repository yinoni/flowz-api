package com.flowzapi.flowz_api_builder.model;

import com.flowzapi.flowz_api_builder.model.flow.FlowDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import static com.flowzapi.flowz_api_builder.model.flow.FlowDTOBuilder.aFlowDTO;

@Data
@Document(collection = "flows")
public class Flow {
    @Id
    private String id;
    private String flowName;
    private String projectId;
    private String ownerId;
    private List<Step> steps;

    public Flow() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public FlowDTO convertToDTO() {
        return aFlowDTO()
                .withFlowName(this.flowName)
                .withId(this.id)
                .withProjectId(this.projectId)
                .build();
    }
}
