package com.flowzapi.flowz_api_builder.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Step {

    @EqualsAndHashCode.Include
    private String id;
    private String url;
    private String title;
    private String body;
    private Map<String, String> headers;
    private String httpMethod;
    private Map<String, String> extract;
    private Map<String, Object> assertions;
    private Map<String, String> routes;
    private Position position;

    @Data
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Position{
        private double x;
        private double y;

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }


}
