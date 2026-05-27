package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.FlowNotFound;
import com.flowzapi.flowz_api_builder.exception.UserNotAllowedException;
import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.flow.FlowDTO;
import com.flowzapi.flowz_api_builder.model.flow.FlowInput;
import com.flowzapi.flowz_api_builder.model.flow.FlowTestResponse;
import com.flowzapi.flowz_api_builder.repos.FlowRepository;
import com.flowzapi.flowz_api_builder.utils.JsonFlattener;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static com.flowzapi.flowz_api_builder.model.FlowBuilder.aFlow;

@Service
public class FlowService {

    @Autowired
    private ObjectMapper objectMapper;
    private JsonFlattener flattener = new JsonFlattener();

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private ProjectService projectService;

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
                .withSteps(List.of())
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

    public void deleteFlowByProjectId(String projectId){
        flowRepository.deleteByProjectId(projectId);
    }

    public void deleteStep(String flowId, String stepId, String userId){
        Flow lookupFlow = this.findById(flowId);
        isUserAllowed(lookupFlow.getOwnerId(), userId);
       List<Step> filteredStepsList = lookupFlow.getSteps().stream().filter(step -> !step.getId().equals(stepId)).toList();
       lookupFlow.setSteps(filteredStepsList);
       flowRepository.save(lookupFlow);
    }

    /**
     *
     * @param flowId - Flow ID
     * @param userId - The current user ID
     * This function execute the steps -> Sends http requests to the URL's in the steps list
     * and execute them according to the step information
     */
    public FlowTestResponse executeSteps(String flowId, String userId){
        Flow lookupFlow = this.findById(flowId);
        Map<String, Object> flowContent = new HashMap<>();

        isUserAllowed(lookupFlow.getOwnerId(), userId);

        for (Step step : lookupFlow.getSteps()) {
            try {
                // 1. מייצרים את הקליינט (ה"דפדפן" הווירטואלי שלנו)
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = buildRequest(step, flowContent);

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                Map<String, Object> mappedBody = objectMapper.readValue(response.body(), Map.class);
                mappedBody.put("status", response.statusCode());
                flowContent.putAll(extractBody(mappedBody, step.getExtract(), step.getAssertions()));
            } catch (Exception e) {
                return new FlowTestResponse("FAILED",  e.getMessage(), false);
            }
        }

        return new FlowTestResponse("COMPLETED",  "", true);
    }

    /**
     *
     * @param step - Step information
     * @param flowContent - All the responses that got from the previous steps
     * @return - New http request for the current step
     */
    public HttpRequest buildRequest(Step step, Map<String, Object> flowContent) {

        String url = step.getUrl();
        String body = step.getBody();

        for (Map.Entry<String, Object> entry : flowContent.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String valueStr = String.valueOf(entry.getValue());

            if (url != null) {
                url = url.replace(placeholder, valueStr);
            }
            if (body != null && !body.isEmpty()) {
                body = body.replace(placeholder, valueStr);
            }
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url));

        if(step.getHeaders() != null) {
            for (Map.Entry<String, String> headerEntry : step.getHeaders().entrySet()) {
                String headerVal = headerEntry.getValue();

                if (headerVal != null) {
                    for (Map.Entry<String, Object> envEntry : flowContent.entrySet()) {
                        headerVal = headerVal.replace("{{" + envEntry.getKey() + "}}", String.valueOf(envEntry.getValue()));
                    }
                }

                requestBuilder.header(headerEntry.getKey(), headerVal);

            }
        }

        HttpRequest.BodyPublisher bodyPublisher = (body != null && !body.isEmpty())
                ? HttpRequest.BodyPublishers.ofString(body)
                : HttpRequest.BodyPublishers.noBody();


        requestBuilder.method(step.getHttpMethod(), bodyPublisher);


        return requestBuilder.build();
    }

    /**
     *
     * @param mappedBody - The body mapped to Map with id - String and Value - Object
     * @param extractorMap - The extractor map
     * @param assertions - The assertions map
     * @return - The body extracted by the extractor map and validated by the assertions map
     */
    public Map<String, Object> extractBody(Map<String, Object> mappedBody, Map<String, String> extractorMap, Map<String, Object> assertions) {
        List<String> errors = new ArrayList<>();

        if(mappedBody.isEmpty())
            return mappedBody;

        Map<String, Object> flattenedMap = flattener.flatten(mappedBody, extractorMap);
        Map<String, Object> extractedBody = new HashMap<>();

        for(Map.Entry<String, Object> assertionEntry : assertions.entrySet()){
            if(!flattenedMap.containsKey(assertionEntry.getKey()))
                errors.add("* expected " + assertionEntry.getKey() + " value to be: " + assertionEntry.getValue() + " but got nothing");
            else{
                String actualValue = String.valueOf(flattenedMap.get(assertionEntry.getKey()));
                String expectedValue = String.valueOf(assertionEntry.getValue());

                if (!Objects.equals(actualValue, expectedValue)) {
                    errors.add("* expected '" + assertionEntry.getKey() + "' value to be: " + expectedValue + " but got: " + actualValue);
                }
            }

        }

        if(!errors.isEmpty())
            throw new RuntimeException(String.join("\n", errors));

        for(Map.Entry<String, String> extractorEntry : extractorMap.entrySet()){
            if(flattenedMap.containsKey(extractorEntry.getValue())){
                extractedBody.put(extractorEntry.getKey(), flattenedMap.get(extractorEntry.getValue()));
            }
        }

        return extractedBody;
    }
}
