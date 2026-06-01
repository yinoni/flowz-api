package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.AuthenticationException;
import com.flowzapi.flowz_api_builder.jwt.JwtService;
import com.flowzapi.flowz_api_builder.model.User;
import com.flowzapi.flowz_api_builder.model.authentication.AuthenticationRequest;
import com.flowzapi.flowz_api_builder.model.authentication.SignUpRequest;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.model.user.UserDTO;
import com.flowzapi.flowz_api_builder.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.flowzapi.flowz_api_builder.model.UserBuilder.anUser;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String login(AuthenticationRequest request) throws AuthenticationException {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(customUserDetails);

        return token;
    }

    public String signup(SignUpRequest request, boolean withGoogle){
        Optional<User> lookupUser = userRepository.findByEmail(request.getEmail());

        if(lookupUser.isPresent())
            throw new AuthenticationException("Email already in use!", HttpStatus.CONFLICT);

        User newUser = anUser()
                .withEmail(request.getEmail())
                .withUsername(request.getUsername())
                .withPassword(passwordEncoder.encode(request.getPassword()))
                .withVerified(withGoogle)
                .withWithGoogle(withGoogle)
                .build();

        newUser = userRepository.save(newUser);

        String jwtToken = jwtService.generateToken(newUser);

        return jwtToken;
    }

    public String authenticateWithGoogle(Map<String, String> userData){
        String email = userData.get("email");
        String username = userData.get("username");
        String password = UUID.randomUUID().toString();
        Optional<User> lookupUser = userRepository.findByEmail(userData.get("email"));

        if(lookupUser.isPresent()) {
            User user = lookupUser.get();
            if(user.isWithGoogle())
                return jwtService.generateToken(user);
            else{
                throw new AuthenticationException("Invalid email or password!", HttpStatus.UNAUTHORIZED);
            }
        }

        return this.signup(new SignUpRequest(email, password, username), true);
    }
}
