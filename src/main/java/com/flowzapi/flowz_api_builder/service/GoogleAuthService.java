package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.AuthenticationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String googleClientId;

    public GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            } else {
                throw new AuthenticationException("Invalid google token", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new AuthenticationException("Failed to verify Google token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    public @Nullable Map<String, Object> getPayloadFromGoogleAccessToken(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            throw new AuthenticationException("Failed to verify Google access token", HttpStatus.UNAUTHORIZED);
        }
    }
}
