package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.FlowNotFound;
import com.flowzapi.flowz_api_builder.exception.UserNotAllowedException;
import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.flow.FlowDTO;
import com.flowzapi.flowz_api_builder.model.flow.FlowEditInput;
import com.flowzapi.flowz_api_builder.model.flow.FlowInput;
import com.flowzapi.flowz_api_builder.model.flow.FlowTestResponse;
import com.flowzapi.flowz_api_builder.repos.FlowRepository;
import com.flowzapi.flowz_api_builder.utils.JsonFlattener;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
        Flow lookupFlow = this.findById(flowId);

        isUserAllowed(lookupFlow.getOwnerId(), userId);

        return lookupFlow.getSteps();
    }

    /**
     *
     * @param flowId - Flow ID
     * @param step - The step information
     * @param userId - The user ID
     *  This function adds new step to the flow with the flowId
     */
    public void addStep(String flowId, Step step, String userId){
        Flow lookupFlow = this.findById(flowId);
        String stepUUID = UUID.randomUUID().toString();
        isUserAllowed(lookupFlow.getOwnerId(), userId);

        step.setId(stepUUID);

        lookupFlow.getSteps().add(step);

        flowRepository.save(lookupFlow);
    }

    public void deleteFlow(String flowId, String userId){
        Flow lookupFlow = this.findById(flowId);
        isUserAllowed(lookupFlow.getOwnerId(), userId);
        flowRepository.delete(lookupFlow);
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
        Flow lookupFlow = this.findById(flowId);
        isUserAllowed(lookupFlow.getOwnerId(), userId);
       List<Step> filteredStepsList = lookupFlow.getSteps().stream().filter(step -> !step.getId().equals(stepId)).toList();
       lookupFlow.setSteps(filteredStepsList);
       flowRepository.save(lookupFlow);
    }

    public void editStep(String flowId, Step step, String userId){
        Flow lookupFlow = this.findById(flowId);
        isUserAllowed(lookupFlow.getOwnerId(), userId);
        List<Step> steps = lookupFlow.getSteps();

        for(int i = 0; i< steps.size(); i++){
            if(steps.get(i).getId().equals(step.getId())){
                steps.set(i, step);
                break;
            }
        }

        flowRepository.save(lookupFlow);
    }

    /**
     * This function edits an existing flow
     * @param flowEditInput - The flow edit input - contains the flowId, the new flow name and the new globalURL
     * @param userId - The ID of the current user
     */
    public void editFlow(FlowEditInput flowEditInput, String userId){
        Flow flow = this.findById(flowEditInput.getId());
        isUserAllowed(flow.getOwnerId(), userId);

        flow.setFlowName(flowEditInput.getFlowName());
        flow.setGlobalURL(flowEditInput.getGlobalURL());
        flow.setGlobalHeaders(flowEditInput.getGlobalHeaders());

        flowRepository.save(flow);
    }


}
