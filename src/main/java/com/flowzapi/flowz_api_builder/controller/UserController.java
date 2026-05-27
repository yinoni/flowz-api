package com.flowzapi.flowz_api_builder.controller;

import com.flowzapi.flowz_api_builder.jwt.JwtService;
import com.flowzapi.flowz_api_builder.model.User;
import com.flowzapi.flowz_api_builder.model.authentication.AuthenticationRequest;
import com.flowzapi.flowz_api_builder.model.authentication.SignUpRequest;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.model.user.UserDTO;
import com.flowzapi.flowz_api_builder.service.ProjectService;
import com.flowzapi.flowz_api_builder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;


    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();


            String token = jwtService.generateToken(customUserDetails);


            return ResponseEntity.ok(token);

        } catch (BadCredentialsException e) {
            // אם הסיסמה או המייל שגויים - מחזירים 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
        UserDTO userDTO = userService.signup(request);

        return ResponseEntity.ok(userDTO);
    }
}
