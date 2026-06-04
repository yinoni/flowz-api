package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.BadRequestException;
import com.flowzapi.flowz_api_builder.exception.FlowNotFound;
import com.flowzapi.flowz_api_builder.exception.UserNotAllowedException;
import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.flow.FlowTestResponse;
import com.flowzapi.flowz_api_builder.model.step.StepWSResponse;
import com.flowzapi.flowz_api_builder.model.step.StepWSResponseBuilder;
import com.flowzapi.flowz_api_builder.repos.FlowRepository;
import com.flowzapi.flowz_api_builder.utils.JsonFlattener;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.flowzapi.flowz_api_builder.model.step.StepWSResponseBuilder.aStepWSResponse;

@Service
@RequiredArgsConstructor
public class FlowExecutionService {

    private final RedisTemplate redisTemplate;
    private final FlowRepository flowRepository;
    private final String REDIS_EXECUTION_ID_KEY = "execution-id:";
    private final ObjectMapper objectMapper;
    private JsonFlattener flattener = new JsonFlattener();
    private final SimpMessagingTemplate messagingTemplate;
    private final String SOCKET_TOPIC_DESTINATION = "/flow-events/";
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public Flow findById(String flowId) {
        return flowRepository.findById(flowId)
                .orElseThrow(() -> new FlowNotFound("The flow is not found"));
    }

    public void isUserAllowed(String flowUserId, String currentUserId){
        if(!flowUserId.equals(currentUserId))
            throw new UserNotAllowedException("User is not allowed to access flow");
    }

    public String getExecutionID(String flowID, String userID){
        Flow flow = flowRepository.findById(flowID).orElseThrow(() ->
                new FlowNotFound("Flow with id: " + flowID + " not found"));

        if(!flow.getOwnerId().equals(userID))
            throw new UserNotAllowedException("User not allowed to do this action");

        String executionID = UUID.randomUUID().toString();
        try {
            String executionValue = objectMapper.writeValueAsString(Map.of("userId", userID, "flowId", flowID));

            redisTemplate.opsForValue().set(REDIS_EXECUTION_ID_KEY + executionID, executionValue, 10, TimeUnit.MINUTES);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to serialize execution context to JSON", e);
        }

        return executionID;
    }

    public void executeFlow(String executionID, String userID){
        try{
            String redisValue = (String) redisTemplate.opsForValue().get(REDIS_EXECUTION_ID_KEY + executionID);

            if(redisValue == null) {
                System.out.println("SOCKET MESSAGE => Error! Execution ID not found or expired.");
                messagingTemplate.convertAndSend(SOCKET_TOPIC_DESTINATION + executionID, (Object) Map.of(
                        "status", "FLOW_FAILED",
                        "success", false,
                        "message", "Execution ID not found or expired."
                ));
                return;
            }

            Map<String, String> mappedValue = objectMapper.readValue(redisValue, Map.class);

            if(mappedValue != null && mappedValue.containsKey("userId") && mappedValue.containsKey("flowId")){
                String flowId = mappedValue.get("flowId");
                String userId = mappedValue.get("userId");

                if(!userId.equals(userID))
                    throw new UserNotAllowedException("User not allowed to do this action");

                messagingTemplate.convertAndSend(SOCKET_TOPIC_DESTINATION + executionID, (Object) Map.of(
                        "status", "FLOW_STARTING",
                        "success", true,
                        "message", "Executing..."
                ));

                FlowTestResponse testResponse = executeSteps(flowId, userID, executionID);

                messagingTemplate.convertAndSend(SOCKET_TOPIC_DESTINATION + executionID, (Object) Map.of(
                        "status", testResponse.getStatus(),
                        "success", testResponse.isTestPassed(),
                        "message", testResponse.getMessage()
                ));
            }
            else {
                messagingTemplate.convertAndSend(SOCKET_TOPIC_DESTINATION + executionID, (Object) Map.of(
                        "status", "FLOW_FAILED",
                        "success", false,
                        "message", "Bad request! Check your parameters."
                ));

                System.out.println("SOCKET MESSAGE => Bad request! Check your parameters.");
            }
        }
        catch(Exception e){
            messagingTemplate.convertAndSend(SOCKET_TOPIC_DESTINATION + executionID, (Object) Map.of(
                    "status", "FLOW_FAILED",
                    "success", false,
                    "message", e.getMessage()
            ));
        }
        finally {
            redisTemplate.delete(REDIS_EXECUTION_ID_KEY + executionID);
        }

    }

    /**
     *
     * @param flowId - Flow ID
     * @param userId - The current user ID
     * This function execute the steps -> Sends http requests to the URL's in the steps list
     * and execute them according to the step information
     */
    public FlowTestResponse executeSteps(String flowId, String userId, String executionID){
        Flow lookupFlow = this.findById(flowId);
        Map<String, Object> flowContent = new HashMap<>();
        if(lookupFlow.getGlobalVariables() != null)
            flowContent.putAll(lookupFlow.getGlobalVariables());

        isUserAllowed(lookupFlow.getOwnerId(), userId);
        StepWSResponseBuilder stepWSResponseBuilder = aStepWSResponse();
        for (Step step : lookupFlow.getSteps()) {
            try {
                stepWSResponseBuilder
                        .withStepId(step.getId())
                        .withMessage("'" + step.getTitle() + "' Test Passed!")
                        .withStatus("STEP_PASSED")
                        .withSuccess(true);

                Map<String, Object> activeAssertions = new HashMap<>();

                if (lookupFlow.getGlobalAssertions() != null) {
                    activeAssertions = objectMapper.convertValue(
                            lookupFlow.getGlobalAssertions(),
                            new TypeReference<Map<String, Object>>() {}
                    );
                }

                if (step.getAssertions() != null) {
                    activeAssertions.putAll(step.getAssertions());
                }

                HttpRequest request = buildRequest(step, flowContent, lookupFlow);

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                Map<String, Object> mappedBody = new HashMap<>();
                String bodyString = response.body() != null ? response.body().trim() : "";

                if (!bodyString.isEmpty()) {
                    if (bodyString.startsWith("{") || bodyString.startsWith("[")) {
                        try {
                            mappedBody = objectMapper.readValue(bodyString, Map.class);
                        } catch (Exception jsonEx) {
                            mappedBody.put("rawValue", bodyString);
                        }
                    } else {
                        mappedBody.put("rawValue", bodyString);
                    }
                }

                boolean haveStatusAssertion = activeAssertions.containsKey("status");
                int resStatusCode = response.statusCode();

                if(!haveStatusAssertion && !(resStatusCode >= 200 && resStatusCode <= 299)){
                    stepWSResponseBuilder.withStatus("STEP_FAILED")
                            .withMessage("'" + step.getTitle() + "' Test Failed!\n   " + response.body())
                            .withSuccess(false);

                    sendWSForStep(stepWSResponseBuilder.build(), executionID);
                    return new FlowTestResponse("FLOW_FAILED", "The response returned with status code: " + resStatusCode,false);

                }

                mappedBody.put("status", resStatusCode);
                flowContent.putAll(extractBody(mappedBody, step.getExtract(), activeAssertions));

                sendWSForStep(stepWSResponseBuilder.build(), executionID);
            } catch (Exception e) {
                stepWSResponseBuilder.withStatus("STEP_FAILED")
                        .withMessage("'" + step.getTitle() + "' Test Failed!\n   " + e.getMessage())
                        .withSuccess(false);
                sendWSForStep(stepWSResponseBuilder.build(), executionID);
                return new FlowTestResponse("FLOW_FAILED", "One of the steps got failed", false);
            }
        }

        return new FlowTestResponse("FLOW_COMPLETED",  "", true);
    }

    /**
     *
     * @param step - Step information
     * @param flowContent - All the responses that got from the previous steps
     * @return - New http request for the current step
     */
    public HttpRequest buildRequest(Step step, Map<String, Object> flowContent, Flow currentFlow) {

        String url = step.getUrl();
        String body = step.getBody();
        Map<String, String> finalHeaders = new HashMap<>();

        if(currentFlow.getGlobalHeaders() != null)
            finalHeaders.putAll(currentFlow.getGlobalHeaders());

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

        if(step.getHeaders() != null)
            finalHeaders.putAll(step.getHeaders());

        for (Map.Entry<String, String> headerEntry : finalHeaders.entrySet()) {
            String headerVal = headerEntry.getValue();

            if (headerVal != null) {
                for (Map.Entry<String, Object> envEntry : flowContent.entrySet()) {
                    headerVal = headerVal.replace("{{" + envEntry.getKey() + "}}", String.valueOf(envEntry.getValue()));
                }
            }

            requestBuilder.header(headerEntry.getKey(), headerVal);
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

    private void sendWSForStep(StepWSResponse stepResponse, String executionID){
        messagingTemplate.convertAndSend(SOCKET_TOPIC_DESTINATION + executionID, (Object) Map.of(
                "status", stepResponse.getStatus(),
                "success", stepResponse.isSuccess(),
                "message", stepResponse.getMessage(),
                stepResponse.getStepId(), stepResponse.isSuccess()
        ));
    }



}
