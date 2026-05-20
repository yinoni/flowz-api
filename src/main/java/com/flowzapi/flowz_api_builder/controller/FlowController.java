package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.StepBuilder;
import com.flowzapi.flowz_api_builder.service.FlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.flowzapi.flowz_api_builder.model.StepBuilder.aStep;

@RestController
@RequestMapping("/flow")
public class FlowController {

    @Autowired
    private FlowService flowService;


    @GetMapping("")
    public ResponseEntity<?> temp(){
        return ResponseEntity.ok("Working!");
    }

    @PostMapping("/execute-temp")
    public ResponseEntity<?> executeTemp(){
        String body = "{ \"name\": \"Yinon\", \"role\": \"developer\" }";

        Map<String, String> headers = Map.of(
                "Accept", "application/json"
        );

        Map<String,String> extract = Map.of(
                "jwtToken", "body.data.jwtToken");

        Map<String, Object> assertions = Map.of("status", 200, "body.success", true);

        Step step = aStep()
                .withUrl("https://jsonplaceholder.typicode.com/posts/1")
                .withTitle("Temp")
                .withHeaders(headers)
                .withHttpMethod("GET")
                .withExtract(extract)
                .withAssertions(assertions)
                .build();


        flowService.executeSteps(List.of(step));
        return ResponseEntity.ok("Working!");
    }

}
