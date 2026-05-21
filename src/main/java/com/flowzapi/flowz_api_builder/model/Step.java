package com.flowzapi.flowz_api_builder.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

public class Step {
    private String url;
    private String title;
    private String body;
    private Map<String, String> headers;
    private String httpMethod;

    // כאן נכנסים המשתנים הדינמיים שאתה רוצה לחלץ
    // מפתח: שם המשתנה (למשל: "savedToken")
    // ערך: הנתיב ב-Response (למשל: "response.body.token")
    private Map<String, String> extract;

    // בדיקות: למשל מפתח "status" וערך "200"
    private Map<String, Object> assertions;

    public Step() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getExtract() {
        return extract;
    }

    public void setExtract(Map<String, String> extract) {
        this.extract = extract;
    }

    public Map<String, Object> getAssertions() {
        return assertions;
    }

    public void setAssertions(Map<String, Object> assertions) {
        this.assertions = assertions;
    }


}
