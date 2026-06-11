package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.exception.AuthenticationException;
import com.flowzapi.flowz_api_builder.jwt.JwtService;
import com.flowzapi.flowz_api_builder.model.authentication.AuthenticationRequest;
import com.flowzapi.flowz_api_builder.model.authentication.AuthenticationResponse;
import com.flowzapi.flowz_api_builder.model.authentication.SignUpRequest;
import com.flowzapi.flowz_api_builder.model.authentication.VerificationRequest;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.model.user.UserDTO;
import com.flowzapi.flowz_api_builder.service.AuthService;
import com.flowzapi.flowz_api_builder.service.GoogleAuthService;
import com.flowzapi.flowz_api_builder.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // מאשר לקליינט שלך לדבר עם השרת
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authService.login(request);

        ResponseCookie refreshTokenCookie = generateResponseCookie(response.getRefreshToken(), 30);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response.getAccessToken());
    }

    @PostMapping("/register")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest request) {
        AuthenticationResponse response = authService.signup(request, false);

        ResponseCookie refreshTokenCookie = generateResponseCookie(response.getRefreshToken(), 30);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response.getAccessToken());
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> googleToken) {
        String idToken = googleToken.get("token");

        if (idToken == null || idToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Token is required");
        }

        GoogleIdToken.Payload payload = googleAuthService.verifyGoogleToken(idToken);

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        AuthenticationResponse response = authService.authenticateWithGoogle(Map.of("email", email, "username", name));

        ResponseCookie refreshTokenCookie = generateResponseCookie(response.getRefreshToken(), 30);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response.getAccessToken());
    }

    @PostMapping("/validate-code")
    public ResponseEntity<?> validateVerificationCode(@Valid @RequestBody VerificationRequest verificationRequest, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String token = authService.validateVerificationCode(verificationRequest.getCode(), customUserDetails.getId());

        return ResponseEntity.ok(token);
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode( @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        authService.resendVerificationCode(customUserDetails.getId(), customUserDetails.getEmail());

        return ResponseEntity.ok("Sent verification code.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refresh_token", required = false) String clientRefreshToken) {
        System.out.println("Refresh Token: " + clientRefreshToken);

        String newAccessToken = authService.refresh(clientRefreshToken);

        return ResponseEntity.ok(newAccessToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refresh_token", required = false) String clientRefreshToken, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        authService.logout(customUserDetails.getId(), clientRefreshToken);
        ResponseCookie refreshTokenCookie = generateResponseCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body("Logged out");
    }

    private ResponseCookie generateResponseCookie(String refreshToken, int age) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(age))
                .sameSite("None")
                .build();
    }
}
