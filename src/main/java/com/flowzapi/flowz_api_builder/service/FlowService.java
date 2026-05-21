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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                flowContent.putAll(extractBody(response.body(), step.getExtract()));
            } catch (Exception e) {
                System.out.println("משהו השתבש בשליחת הבקשה: " + e.getMessage());
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

    public Map<String, Object> extractBody(String body, Map<String, String> extractorMap){
        Map<String, Object> mappedBody = objectMapper.readValue(body, Map.class);

        if(mappedBody.isEmpty())
            return mappedBody;

        Map<String, Object> flattenedMap = flattener.flatten(mappedBody, extractorMap);
        Map<String, Object> extractedBody = new HashMap<>();

        for(Map.Entry<String, String> entry : extractorMap.entrySet()){
            if(flattenedMap.containsKey(entry.getValue())){
                extractedBody.put(entry.getKey(), flattenedMap.get(entry.getValue()));
            }
        }

        return extractedBody;
    }
}
