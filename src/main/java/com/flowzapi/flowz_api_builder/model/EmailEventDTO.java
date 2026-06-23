package com.flowzapi.flowz_api_builder.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmailEventDTO {
    private String toEmail;
    private String verificationCode;
}
