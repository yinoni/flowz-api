package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.FlowNotFound;
import com.flowzapi.flowz_api_builder.exception.UserNotAllowedException;
import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.flow.*;
import com.flowzapi.flowz_api_builder.repos.FlowRepository;
import com.flowzapi.flowz_api_builder.repos.projections.FlowOwnerIdProjection;
import com.flowzapi.flowz_api_builder.repos.projections.FlowStepsProjection;
import com.flowzapi.flowz_api_builder.utils.JsonFlattener;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

import static com.flowzapi.flowz_api_builder.model.FlowBuilder.aFlow;

@Service
@RequiredArgsConstructor
public class FlowService {
    private final FlowRepository flowRepository;
    private final ProjectService projectService;
    private final MongoTemplate mongoTemplate;

    public Flow findById(String flowId) {
        return flowRepository.findById(flowId)
                .orElseThrow(() -> new FlowNotFound("The flow is not found"));
    }

    public void isUserAllowed(String flowUserId, String currentUserId){
        if(!flowUserId.equals(currentUserId))
            throw new UserNotAllowedException("User is not allowed to access flow");
    }

    /**
     *
     * @param flowInput - Contains the flow name and the project ID
     * @return - Saves this flow with the flow name from the flowInput var and returns
     * flow DTO that contains all the new flow data
     */
    public FlowDTO createFlow(FlowInput flowInput, String userId) {
        Project project = projectService.findById(flowInput.getProjectId(), userId);


        Flow flow = aFlow()
                .withFlowName(flowInput.getFlowName())
                .withProjectId(project.getId())
                .withOwnerId(userId)
                .withSteps(new ArrayList<>())
                .withGlobalURL(flowInput.getGlobalURL())
                .withGlobalVariables(flowInput.getGlobalVariables())
                .withGlobalHeaders(flowInput.getGlobalHeaders())
                .build();

        return flowRepository.save(flow).convertToDTO();
    }

    /**
     *
     * @param flowId - The flow id
     * @param userId - The current user ID
     * @return - Returns the flow information by its ID
     */
    public FlowDTO getFlow(String flowId, String userId){
        Flow flow = this.findById(flowId);

        isUserAllowed(flow.getOwnerId(), userId);

        return flow.convertToDTO();
    }

    /**
     *
     * @param projectId - The ID of the project
     * @param userId - The current user ID
     * @return - All the flows of the project with projectId
     */
    public List<FlowDTO> getFlowsByProjectId(String projectId, String userId){
        Project project = projectService.findById(projectId, userId);

        List<Flow> flows = flowRepository.findByProjectId(projectId);

        return flows.stream().map(f -> f.convertToDTO()).toList();
    }

    /**
     *
     * @param flowId - The flow ID
     * @param userId - The user ID
     * @return - Returns the flow steps
     */
    public List<Step> getFlowSteps(String flowId, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId);
        isUserAllowed(stepsProjection.getOwnerId(), userId);

        return stepsProjection.getSteps();
    }

    /**
     *
     * @param flowId - Flow ID
     * @param step - The step information
     * @param userId - The user ID
     *  This function adds new step to the flow with the flowId
     */
    public void addStep(String flowId, Step step, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId);
        isUserAllowed(stepsProjection.getOwnerId(), userId);
        String stepUUID = UUID.randomUUID().toString();


        step.setId(stepUUID);

        Query query = new Query(Criteria.where("id").is(flowId));
        Update update = new Update().push("steps", step);

        mongoTemplate.updateFirst(query, update, Flow.class);
    }

    public void deleteFlow(String flowId, String userId){
        FlowOwnerIdProjection lookupFlow = flowRepository.findOwnerIdProjectedById(flowId)
                .orElseThrow(() -> new FlowNotFound("This flow is not exists!"));

        isUserAllowed(lookupFlow.getOwnerId(), userId);

        flowRepository.deleteById(flowId);
    }

    /**
     * This function deletes all the flows of a project
     * @param projectId - The project ID
     */
    public void deleteFlowByProjectId(String projectId){
        flowRepository.deleteByProjectId(projectId);
    }

    /**
     * This function deletes step from flow
     * @param flowId - The ID of the flow
     * @param stepId - The ID of the Step
     * @param userId - The current user ID
     */
    public void deleteStep(String flowId, String stepId, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId);
        isUserAllowed(stepsProjection.getOwnerId(), userId);

        Query query = new Query(Criteria.where("id").is(flowId));
        Update update = new Update().pull("steps", Query.query(Criteria.where("id").is(stepId)));

        mongoTemplate.updateFirst(query, update, Flow.class);

    }

    public void editStep(String flowId, Step step, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId);
        isUserAllowed(stepsProjection.getOwnerId(), userId);

        Query query = new Query(Criteria.where("id").is(flowId).and("steps.id").is(step.getId()));
        Update update = new Update();

        update.set("steps.$",  step);

        mongoTemplate.updateFirst(query, update, Flow.class);
    }

    /**
     * This function edits an existing flow
     * @param flowEditInput - The flow edit input - contains the flowId, the new flow name and the new globalURL
     * @param userId - The ID of the current user
     */
    public void editFlow(FlowEditInput flowEditInput, String userId){
        FlowOwnerIdProjection ownerIdProjection = flowRepository.findOwnerIdProjectedById(flowEditInput.getId())
                        .orElseThrow(() -> new FlowNotFound("This flow is not exists!"));

        isUserAllowed(ownerIdProjection.getOwnerId(), userId);

        Query query = new Query(Criteria.where("id").is(flowEditInput.getId()));
        Update update = new Update()
                .set("flowName", flowEditInput.getFlowName())
                .set("globalURL", flowEditInput.getGlobalURL())
                .set("globalVariables", flowEditInput.getGlobalVariables())
                .set("globalHeaders", flowEditInput.getGlobalHeaders());


        mongoTemplate.updateFirst(query, update, Flow.class);
    }

    public void setGlobals(SetGlobalsRequest setGlobalsRequest, String flowId, String userId){
        FlowOwnerIdProjection ownerIdProjection = flowRepository.findOwnerIdProjectedById(flowId)
                .orElseThrow(() -> new FlowNotFound("This flow is not exists!"));
        isUserAllowed(ownerIdProjection.getOwnerId(), userId);

        String fieldName = setGlobalsRequest.getFieldName().toDbName();

        Query query = new Query(Criteria.where("id").is(flowId));
        Update update = new Update().set(fieldName, setGlobalsRequest.getGlobals());

        mongoTemplate.updateFirst(query, update, Flow.class);
        Flow flow = findById(flowId);
    }

    private FlowStepsProjection findStepsProjectedById(String flowId) {
        return flowRepository.findStepsProjectedById(flowId)
                .orElseThrow(() -> new FlowNotFound("This flow is not exists!"));
    }

    public void reorderSteps(String flowId, ReorderStepsRequest stepsRequest, String userId){
        FlowStepsProjection flowStepsProjection = findStepsProjectedById(flowId);

        isUserAllowed(flowStepsProjection.getOwnerId(), userId);

        List<Step> currentSteps = flowStepsProjection.getSteps();

        // 1. ⚡ הופכים את הרשימה ל-Map שבו המפתח הוא ה-ID והערך הוא אובייקט ה-Step
        // הפעולה הזו רצה פעם אחת בלבד ב-O(N)
        Map<String, Step> stepMap = currentSteps.stream()
                .collect(Collectors.toMap(Step::getId, step -> step));

        List<Step> newOrderSteps = new ArrayList<>();

        for(String id : stepsRequest.getSteps()){
            Step currentStep = stepMap.get(id);
            if (currentStep != null) {
                newOrderSteps.add(currentStep);
            }
        }

        Query query = new Query(Criteria.where("id").is(flowId));
        Update update = new Update().set("steps", newOrderSteps);

        mongoTemplate.updateFirst(query, update, Flow.class);
    }
}
