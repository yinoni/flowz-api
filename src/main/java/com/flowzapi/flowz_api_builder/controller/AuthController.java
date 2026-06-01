package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.jwt.JwtService;
import com.flowzapi.flowz_api_builder.model.authentication.AuthenticationRequest;
import com.flowzapi.flowz_api_builder.model.authentication.SignUpRequest;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.model.user.UserDTO;
import com.flowzapi.flowz_api_builder.service.AuthService;
import com.flowzapi.flowz_api_builder.service.GoogleAuthService;
import com.flowzapi.flowz_api_builder.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // מאשר לקליינט שלך לדבר עם השרת
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        String token = authService.login(request);

        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
        String token = authService.signup(request, false);

        return ResponseEntity.ok(token);
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

        String token = authService.authenticateWithGoogle(Map.of("email", email, "username", name));

        return ResponseEntity.ok(token);
    }
}
