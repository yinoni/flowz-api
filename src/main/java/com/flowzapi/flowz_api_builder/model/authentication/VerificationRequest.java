package com.flowzapi.flowz_api_builder.model.authentication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class VerificationRequest {
    private String code;
}
