package com.flowzapi.flowz_api_builder.model;

import java.util.Map;

public final class StepBuilder {
    private String url;
    private String title;
    private Map<String, String> body;
    private Map<String, String> headers;
    private String flowID;
    private String httpMethod;
    private Map<String, String> extract;
    private Map<String, Object> assertions;

    private StepBuilder() {
    }

    public static StepBuilder aStep() {
        return new StepBuilder();
    }

    public StepBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public StepBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public StepBuilder withBody(Map<String, String> body) {
        this.body = body;
        return this;
    }

    public StepBuilder withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public StepBuilder withFlowID(String flowID) {
        this.flowID = flowID;
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

    public Step build() {
        Step step = new Step();
        step.setUrl(url);
        step.setTitle(title);
        step.setBody(body);
        step.setHeaders(headers);
        step.setFlowID(flowID);
        step.setHttpMethod(httpMethod);
        step.setExtract(extract);
        step.setAssertions(assertions);
        return step;
    }
}
