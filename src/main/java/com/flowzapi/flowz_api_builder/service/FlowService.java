package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.FlowNotFound;
import com.flowzapi.flowz_api_builder.exception.UserNotAllowedException;
import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.flow.*;
import com.flowzapi.flowz_api_builder.model.step.StepRequest;
import com.flowzapi.flowz_api_builder.repos.FlowRepository;
import com.flowzapi.flowz_api_builder.repos.projections.FlowOwnerIdProjection;
import com.flowzapi.flowz_api_builder.repos.projections.FlowStepsProjection;
import com.flowzapi.flowz_api_builder.utils.JsonFlattener;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
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
import java.time.Instant;
import java.time.LocalDateTime;
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
                .withGlobalAssertions(new HashMap<>())
                .withLastModified(Instant.now())
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
    public FlowStepsResponse getFlowSteps(String flowId, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId);
        isUserAllowed(stepsProjection.getOwnerId(), userId);

        FlowStepsResponse stepsResponse = new FlowStepsResponse(stepsProjection.getSteps(), stepsProjection.getFallbacks());
        return stepsResponse;
    }

    /**
     *
     * @param flowId - Flow ID
     * @param stepRequest - The step information (Including the step group -> fallbacks or steps)
     * @param userId - The user ID
     *  This function adds new step to the flow with the flowId
     */
    public String addStep(String flowId, StepRequest stepRequest, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId);
        isUserAllowed(stepsProjection.getOwnerId(), userId);
        String stepUUID = UUID.randomUUID().toString();
        Step step = stepRequest.getStep();
        String fieldName = stepRequest.getStepGroup().toDbField();

        step.setId(stepUUID);

        Query query = new Query(Criteria.where("id").is(flowId));
        Update update = new Update().push(fieldName, step)
                .set("lastModified", Instant.now());

        mongoTemplate.updateFirst(query, update, Flow.class);

        return stepUUID;
    }

    public void deleteFlow(String flowId, String userId){
        findOwnerIdProjectedById(flowId, userId);

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
        Update update = new Update().pull("steps", Query.query(Criteria.where("id").is(stepId)))
                .set("lastModified", Instant.now());

        mongoTemplate.updateFirst(query, update, Flow.class);

    }

    public void editStep(String flowId, StepRequest stepRequest, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId);
        isUserAllowed(stepsProjection.getOwnerId(), userId);

        String stepField = stepRequest.getStepGroup().toDbField();
        Step step = stepRequest.getStep();

        Query query = new Query(Criteria.where("id").is(flowId).and(stepField+".id").is(step.getId()));
        Update update = new Update();

        update.set(stepField+".$",  step)
                .set("lastModified", Instant.now());

        mongoTemplate.updateFirst(query, update, Flow.class);
    }

    /**
     * This function edits an existing flow
     * @param flowEditInput - The flow edit input - contains the flowId, the new flow name and the new globalURL
     * @param userId - The ID of the current user
     */
    public void editFlow(FlowEditInput flowEditInput, String userId){
        findOwnerIdProjectedById(flowEditInput.getId(), userId);

        Query query = new Query(Criteria.where("id").is(flowEditInput.getId()));
        Update update = new Update()
                .set("flowName", flowEditInput.getFlowName())
                .set("globalURL", flowEditInput.getGlobalURL())
                .set("globalVariables", flowEditInput.getGlobalVariables())
                .set("globalHeaders", flowEditInput.getGlobalHeaders())
                .set("lastModified", Instant.now());


        mongoTemplate.updateFirst(query, update, Flow.class);
    }

    public void setGlobals(SetGlobalsRequest setGlobalsRequest, String flowId, String userId){
        findOwnerIdProjectedById(flowId, userId);

        String fieldName = setGlobalsRequest.getFieldName().toDbName();

        Query query = new Query(Criteria.where("id").is(flowId));
        Update update = new Update().set(fieldName, setGlobalsRequest.getGlobals())
                .set("lastModified", Instant.now());

        mongoTemplate.updateFirst(query, update, Flow.class);
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

        for(ReorderStepsRequest.ReorderItem reorderItem : stepsRequest.getSteps()){
            String stepId = reorderItem.getId();
            Step.Position stepPosition = reorderItem.getPosition();
            Step currentStep = stepMap.get(stepId);
            if (currentStep != null) {
                currentStep.setPosition(stepPosition);
                newOrderSteps.add(currentStep);
            }
        }

        Query query = new Query(Criteria.where("id").is(flowId));
        Update update = new Update().set("steps", newOrderSteps)
                .set("lastModified", Instant.now());

        mongoTemplate.updateFirst(query, update, Flow.class);
    }

    private void findOwnerIdProjectedById(String flowId, String userId) {
        FlowOwnerIdProjection flowOwnerIdProjection = flowRepository.findOwnerIdProjectedById(flowId)
                .orElseThrow(() -> new FlowNotFound("This flow is not exists!"));
        isUserAllowed(flowOwnerIdProjection.getOwnerId(), userId);
    }

    public void deleteFallback(String flowId, String fallbackId, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId);
        isUserAllowed(stepsProjection.getOwnerId(), userId);
        Query query = new Query(Criteria.where("id").is(flowId));

        MongoExpression updateStepsExpression = () -> Document.parse(
                "{$map: {" +
                        "  input: '$steps'," +
                        "  as: 'step'," +
                        "  in: {$mergeObjects: [" +
                        "    '$$step'," +
                        "    {routes: {$arrayToObject: {$filter: {" +
                        "      input: {$objectToArray: '$$step.routes'}," +
                        "      cond: {$ne: ['$$this.v', '" + fallbackId + "']}" +
                        "    }}}}" +
                        "  ]}" +
                        "}}"
        );

        AggregationUpdate update = AggregationUpdate.update()
                .set("fallbacks").toValue((MongoExpression) () -> Document.parse(
                        "{$filter: { input: '$fallbacks', as: 'fb', cond: {$ne: ['$$fb._id', '" + fallbackId + "']} }}"
                ))
                .set("steps").toValue(updateStepsExpression)
                .set("lastModified").toValue(Instant.now());


        mongoTemplate.updateFirst(query, update, Flow.class);

    }

    public enum StepGroup{
        FALLBACKS,
        STEPS;

        public String toDbField(){
            String field = "steps";
            switch (this) {
                case FALLBACKS:
                    field= "fallbacks";
                    break;
                case STEPS:
                    field = "steps";
                    break;
                default:
                    field = "steps";
                    break;
            }

            return field;
        }
    }
}
