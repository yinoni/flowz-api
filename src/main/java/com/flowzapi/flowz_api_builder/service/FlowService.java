package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.utils.JsonFlattener;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class FlowService {
    @Autowired
    private ObjectMapper objectMapper;
    private JsonFlattener flattener = new JsonFlattener();

    public void executeSteps(List<Step> steps){

        Map<String, Object> flowContent = new HashMap<>();

        for (Step step : steps) {
            try {
                // 1. מייצרים את הקליינט (ה"דפדפן" הווירטואלי שלנו)
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = buildRequest(step, flowContent);

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                Map<String, Object> mappedBody = objectMapper.readValue(response.body(), Map.class);
                mappedBody.put("status", response.statusCode());
                flowContent.putAll(extractBody(mappedBody, step.getExtract(), step.getAssertions()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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

    //public List<String> isPassedAssertions()



    public Map<String, Object> extractBody(Map<String, Object> mappedBody, Map<String, String> extractorMap, Map<String, Object> assertions) {
        List<String> errors = new ArrayList<>();

        if(mappedBody.isEmpty())
            return mappedBody;

        Map<String, Object> flattenedMap = flattener.flatten(mappedBody, extractorMap);
        Map<String, Object> extractedBody = new HashMap<>();

        for(Map.Entry<String, Object> assertionEntry : assertions.entrySet()){
            if(!flattenedMap.containsKey(assertionEntry.getKey()))
                errors.add("Error! expected " + assertionEntry.getKey() + " value to be: " + assertionEntry.getValue() + " but got nothing");
            else{
                String actualValue = String.valueOf(flattenedMap.get(assertionEntry.getKey()));
                String expectedValue = String.valueOf(assertionEntry.getValue());

                if (!Objects.equals(actualValue, expectedValue)) {
                    errors.add("Error! expected '" + assertionEntry.getKey() + "' value to be: " + expectedValue + " but got: " + actualValue);
                }
            }

        }

        if(!errors.isEmpty())
            throw new RuntimeException("Assertion failed: " + errors);

        for(Map.Entry<String, String> extractorEntry : extractorMap.entrySet()){
            if(flattenedMap.containsKey(extractorEntry.getValue())){
                extractedBody.put(extractorEntry.getKey(), flattenedMap.get(extractorEntry.getValue()));
            }
        }

        return extractedBody;
    }
}
