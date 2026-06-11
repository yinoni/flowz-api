package com.flowzapi.flowz_api_builder.model;

import java.util.Map;

public final class StepBuilder {
    private String id;
    private String url;
    private String title;
    private String body;
    private Map<String, String> headers;
    private String httpMethod;
    private Map<String, String> extract;
    private Map<String, Object> assertions;
    private Map<String, String> routes;
    private Step.Position position;

    private StepBuilder() {
    }

    public static StepBuilder aStep() {
        return new StepBuilder();
    }

    public StepBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public StepBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public StepBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public StepBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public StepBuilder withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public StepBuilder withHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public StepBuilder withExtract(Map<String, String> extract) {
        this.extract = extract;
        return this;
    }

    public StepBuilder withAssertions(Map<String, Object> assertions) {
        this.assertions = assertions;
        return this;
    }

    public StepBuilder withRoutes(Map<String, String> routes) {
        this.routes = routes;
        return this;
    }

    public StepBuilder withPosition(Step.Position position) {
        this.position = position;
        return this;
    }

    public Step build() {
        Step step = new Step();
        step.setId(id);
        step.setUrl(url);
        step.setTitle(title);
        step.setBody(body);
        step.setHeaders(headers);
        step.setHttpMethod(httpMethod);
        step.setExtract(extract);
        step.setAssertions(assertions);
        step.setRoutes(routes);
        step.setPosition(position);
        return step;
    }
}
