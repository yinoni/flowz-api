package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.*;
import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.flow.FlowTestResponse;
import com.flowzapi.flowz_api_builder.model.flow.FlowTestResponseBuilder;
import com.flowzapi.flowz_api_builder.model.step.StepWSResponse;
import com.flowzapi.flowz_api_builder.model.step.StepWSResponseBuilder;
import com.flowzapi.flowz_api_builder.repos.FlowRepository;
import com.flowzapi.flowz_api_builder.utils.JsonFlattener;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.flowzapi.flowz_api_builder.model.StepBuilder.aStep;
import static com.flowzapi.flowz_api_builder.model.flow.FlowTestResponseBuilder.aFlowTestResponse;
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

    @Async
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

                boolean flowFailed = execute(flowId, userID, executionID);

                FlowTestResponseBuilder flowTestResponseBuilder = aFlowTestResponse()
                        .withTestPassed(flowFailed)
                        .withMessage(flowFailed ? "Execution Failed" : "Execution Succeeded")
                        .withStatus(flowFailed ? "FLOW_FAILED" : "FLOW_PASSED");

                FlowTestResponse testResponse = flowTestResponseBuilder.build();

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
    public boolean execute(String flowId, String userId, String executionID){
        Flow lookupFlow = this.findById(flowId);
        Map<String, Object> flowContent = new HashMap<>();
        boolean flowFailed = false;


        if(lookupFlow.getGlobalVariables() != null)
            flowContent.putAll(lookupFlow.getGlobalVariables());

        isUserAllowed(lookupFlow.getOwnerId(), userId);
        StepWSResponseBuilder stepWSResponseBuilder = aStepWSResponse();

        List<Step> stepsList = lookupFlow.getSteps();
        List<Step> fallbacksList = lookupFlow.getFallbacks();

        Step currentStep = stepsList.get(0);
        Map<String, Step> stepsMap = new HashMap<>();
        Map<String, Step> fallbackStepsMap = new HashMap<>();
        int currentStepIndex = 0;
        boolean retry = false;
        int retryCount = 0;
        Map<String, Object> activeAssertions = new HashMap<>();

        stepsList.forEach((step) -> {
            stepsMap.put(step.getId(), step);
        });

        fallbacksList.forEach((step) -> {
            fallbackStepsMap.put(step.getId(), step);
        });

        while(currentStep != null){
            try{
                stepWSResponseBuilder
                        .withStepId(currentStep.getId())
                        .withMessage("'" + currentStep.getTitle() + "' Test Passed!")
                        .withStatus("STEP_PASSED")
                        .withSuccess(true);

                //Insert the global assertions to the flow execution
                if (lookupFlow.getGlobalAssertions() != null) {
                    activeAssertions = objectMapper.convertValue(
                            lookupFlow.getGlobalAssertions(),
                            new TypeReference<Map<String, Object>>() {}
                    );
                }

                //Insert the current step assertions to the flow execution
                if (currentStep.getAssertions() != null) {
                    activeAssertions.putAll(currentStep.getAssertions());
                }

                //Build the HTTP request
                HttpRequest request = buildRequest(currentStep, flowContent, lookupFlow);

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                //Fetch the status code from the response
                String statusCode = String.valueOf(response.statusCode());
                //Get the next step id from the current step routes by the status code
                String nextStepId = currentStep.getRoutes().get(statusCode);

                Map<String, Object> mappedBody = mapResponseBody(response);

                if(Objects.nonNull(nextStepId)){
                    if(!statusCode.startsWith("2")) {
                        if (retryCount >= 1) {
                            System.err.println("Max retries reached for step: " + currentStep.getId() + ". Stopping flow.");
                            throw new StepExecutionException("Max retries reached for step: " + currentStep.getId() + ". Stopping flow.", HttpStatus.BAD_REQUEST);
                        }
                        currentStep = fallbackStepsMap.get(nextStepId);
                        if(currentStep == null) {
                            throw new StepExecutionException("No fallback step found with this ID: " + nextStepId, HttpStatus.BAD_REQUEST);
                        }
                        stepWSResponseBuilder.withStatus("STEP_FAILED")
                                .withMessage("Enter fallback: " + currentStep.getTitle())
                                .withSuccess(false)
                                .withResponse(response.body());
                        sendWSForStep(stepWSResponseBuilder.build(), executionID);

                        retry = true;
                        retryCount++;
                    }
                    else{
                        //Extract the response body and add it to the flowContent
                        flowContent.putAll(extractBody(mappedBody, currentStep.getExtract(), activeAssertions));

                        currentStep = stepsMap.get(nextStepId);
                        if (currentStep != null) {
                            currentStepIndex = stepsList.indexOf(currentStep);
                        }
                        retry = false;
                        retryCount = 0;
                    }
                }
                else{
                    //Check if there is assertion for status code
                    boolean haveStatusAssertion = activeAssertions.containsKey("status");
                    int resStatusCode = response.statusCode();
                    stepWSResponseBuilder.withResponse(response.body());

                    if(!haveStatusAssertion && !(resStatusCode >= 200 && resStatusCode <= 299)){
                        throw new StepExecutionException("'" + currentStep.getTitle() + "' Test Failed!\n   " + response.body(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    //Extract the response body and add it to the flowContent
                    flowContent.putAll(extractBody(mappedBody, currentStep.getExtract(), activeAssertions));
                    sendWSForStep(stepWSResponseBuilder.build(), executionID);
                    if(retry){
                        currentStep = stepsList.get(currentStepIndex);
                        retry = false;
                        continue;
                    }

                    if (currentStepIndex < stepsList.size() - 1) {
                        currentStepIndex += 1;
                        currentStep = stepsList.get(currentStepIndex);
                        retryCount = 0;
                    }
                    else
                        currentStep = null;
                }
            } catch (Exception e) {
                if(currentStep != null) {
                    stepWSResponseBuilder.withStatus("STEP_FAILED")
                            .withMessage("'" + currentStep.getTitle() + "' Test Failed!\n   " + e.getMessage())
                            .withSuccess(false)
                            .withResponse(e.getMessage());

                    sendWSForStep(stepWSResponseBuilder.build(), executionID);
                    currentStep = null;
                    flowFailed = true;
                }
            }
        }

        return flowFailed;
    }

    public Map<String, Object> mapResponseBody(HttpResponse<String> responseBody){
        String bodyString = responseBody.body() != null ? responseBody.body().trim() : "";
        Map<String, Object> mappedBody = new HashMap<>();

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

        return mappedBody;
    }


    public void validateUrl(String urlString) {
        try {
            URI uri = new URI(urlString);
            String host = uri.getHost();

            if (host == null) {
                throw new URLNotValidException("Invalid URL structure");
            }

            // 1. הגנה בסיסית מפני מילים שמורות
            String lowerHost = host.toLowerCase();
            if (lowerHost.equals("localhost") || lowerHost.endsWith(".local")) {
                throw new URLNotValidException("Access to local network is forbidden");
            }

            // 2. הגנה מתקדמת: פתרון ה-IP (מונע מתוקף להזין פשוט 127.0.0.1)
            InetAddress inetAddress = InetAddress.getByName(host);

            if (inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isAnyLocalAddress()) {
                throw new URLNotValidException("Access to private/loopback IP addresses is forbidden");
            }

        } catch (Exception e) {
            throw new URLNotValidException("URL Validation failed: " + e.getMessage());
        }
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

        if(step.getHeaders() != null)
            finalHeaders.putAll(step.getHeaders());

        for (Map.Entry<String, Object> entry : flowContent.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String valueStr = String.valueOf(entry.getValue());

            if (url != null) {
                url = url.replace(placeholder, valueStr);
            }
            if (body != null && !body.isEmpty()) {
                body = body.replace(placeholder, valueStr);
            }

            finalHeaders.replaceAll((key, val) -> val != null ? val.replace(placeholder, valueStr) : null);
        }

        validateUrl(url);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url));

        finalHeaders.forEach(requestBuilder::header);

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
                stepResponse.getStepId(), stepResponse.isSuccess(),
                "response", stepResponse.getResponse()
        ));
    }



}
