package com.flowzapi.flowz_api_builder.model.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class VerificationRequest {
    @NotBlank(message = "Code is required")
    @NotNull(message = "Code is required")
    private String code;
}
