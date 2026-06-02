package com.flowzapi.flowz_api_builder.model;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

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

    // כאן נכנסים המשתנים הדינמיים שאתה רוצה לחלץ
    // מפתח: שם המשתנה (למשל: "savedToken")
    // ערך: הנתיב ב-Response (למשל: "response.body.token")
    private Map<String, String> extract;

    // בדיקות: למשל מפתח "status" וערך "200"
    private Map<String, Object> assertions;

}
