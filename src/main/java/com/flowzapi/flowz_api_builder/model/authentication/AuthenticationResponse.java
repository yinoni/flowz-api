package com.flowzapi.flowz_api_builder.model.authentication;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Data
@Getter
@Service
@NoArgsConstructor
public class AuthenticationResponse {
    private String refreshToken;
    private String accessToken;

    public AuthenticationResponse(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }
}
