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
import java.util.List;
import java.util.Map;

@Service
public class FlowService {
    @Autowired
    private ObjectMapper objectMapper;
    private JsonFlattener flattener = new JsonFlattener();

    public void executeSteps(List<Step> steps){
        for (Step step : steps) {
            try {
                // 1. מייצרים את הקליינט (ה"דפדפן" הווירטואלי שלנו)
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = buildRequest(step);

                // 3. שולחים את הבקשה ומחכים לתשובה (Response)
                // אמרנו לו לקבל את ה-Body של התשובה כטקסט פשוט (String)
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                extractBody(response.body());

            } catch (Exception e) {
                System.out.println("משהו השתבש בשליחת הבקשה: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public HttpRequest buildRequest(Step step) {
        String body = step.getBody();
        HttpRequest.BodyPublisher bodyPublisher = (body != null && !body.isEmpty())
                ? HttpRequest.BodyPublishers.ofString(body)
                : HttpRequest.BodyPublishers.noBody();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(step.getUrl()))
                .method(step.getHttpMethod(), bodyPublisher);

        if (step.getHeaders() != null) {
            for (Map.Entry<String, String> entry : step.getHeaders().entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        return requestBuilder.build();
    }

    public void extractBody(String body){
        Map<String, Object> mappedBody = objectMapper.readValue("{\"status\":\"success\",\"user\":{\"profile\":{\"name\":\"Yinon\",\"role\":\"developer\"}}}", Map.class);
        flattener.flatten(mappedBody);

    }
}
