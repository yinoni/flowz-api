package com.flowzapi.flowz_api_builder.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
public class Step {
    private String id;
    private String url;
    private String title;
    private Map<String, String> body;
    private Map<String, String> headers;
    private String flowID;
    private String httpMethod;

    // כאן נכנסים המשתנים הדינמיים שאתה רוצה לחלץ
    // מפתח: שם המשתנה (למשל: "savedToken")
    // ערך: הנתיב ב-Response (למשל: "response.body.token")
    private Map<String, String> extract;

    // בדיקות: למשל מפתח "status" וערך "200"
    private Map<String, Object> assertions;

    public Step() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Map<String, String> getBody() {
        return body;
    }

    public void setBody(Map<String, String> body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getFlowID() {
        return flowID;
    }

    public void setFlowID(String flowID) {
        this.flowID = flowID;
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
