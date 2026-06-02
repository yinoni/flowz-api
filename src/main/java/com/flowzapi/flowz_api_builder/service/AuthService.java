package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.AuthenticationException;
import com.flowzapi.flowz_api_builder.exception.InvalidVerificationException;
import com.flowzapi.flowz_api_builder.exception.UserNotAllowedException;
import com.flowzapi.flowz_api_builder.jwt.JwtService;
import com.flowzapi.flowz_api_builder.model.User;
import com.flowzapi.flowz_api_builder.model.authentication.AuthenticationRequest;
import com.flowzapi.flowz_api_builder.model.authentication.AuthenticationResponse;
import com.flowzapi.flowz_api_builder.model.authentication.SignUpRequest;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import com.flowzapi.flowz_api_builder.model.user.UserDTO;
import com.flowzapi.flowz_api_builder.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.flowzapi.flowz_api_builder.model.UserBuilder.anUser;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate redisTemplate;
    private final String VERIFICATION_KEY_REDIS = "verificationKey:";
    private final String REDIS_REFRESH_TOKEN = "refreshToken:";
    private final EmailService emailService;
    private final ObjectMapper objectMapper;


    public AuthenticationResponse login(AuthenticationRequest request) throws AuthenticationException {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        if(!customUserDetails.isVerified()){
            try{
                resendVerificationCode(customUserDetails.getId(), customUserDetails.getEmail());
            }
            catch(InvalidVerificationException e){
                System.out.println("There is already available code");
            }
        }

        String accessToken = jwtService.generateToken(customUserDetails);
        String refreshToken = createRefreshToken(customUserDetails.getId());

        return new AuthenticationResponse(refreshToken, accessToken);
    }

    public AuthenticationResponse signup(SignUpRequest request, boolean withGoogle){
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

        if(!withGoogle)
            sendVerificationCode(newUser.getId(), newUser.getEmail());

        String accessToken = jwtService.generateToken(newUser);
        String refreshToken = createRefreshToken(newUser.getId());

        return new AuthenticationResponse(refreshToken, accessToken);
    }

    private String createRefreshToken(String userId){
        String refreshToken = UUID.randomUUID().toString();
        String redisKey = REDIS_REFRESH_TOKEN + refreshToken;

        redisTemplate.opsForValue().set(redisKey, userId, 30, TimeUnit.DAYS);

        return refreshToken + ":" + userId;
    }

    public String refresh(String clientRefreshToken){

        if (clientRefreshToken == null || clientRefreshToken.isEmpty()) {
            throw new AuthenticationException("Refresh token is missing!", HttpStatus.BAD_REQUEST);
        }

        String[] splitRefreshToken = clientRefreshToken.split(":");
        if(splitRefreshToken.length != 2)
            throw new AuthenticationException("Invalid refresh token!", HttpStatus.BAD_REQUEST);

        String refreshToken = splitRefreshToken[0];
        String userId = splitRefreshToken[1];

        validateRefreshToken(refreshToken, userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotAllowedException("User not found!"));

        return jwtService.generateToken(user);
    }

    private void validateRefreshToken(String refreshToken, String userId){
        String redisKey = REDIS_REFRESH_TOKEN + refreshToken;
        String redisValue = (String) redisTemplate.opsForValue().get(redisKey);

        if(redisValue == null || !redisValue.equals(userId))
            throw new AuthenticationException("Refresh token expired or invalid!", HttpStatus.UNAUTHORIZED);
    }

    private void sendVerificationCode(String userId, String email){
        String verificationCode = generate4DigitCode();
        redisTemplate.opsForValue().set(VERIFICATION_KEY_REDIS + userId, verificationCode, 2, TimeUnit.MINUTES);
        System.out.println("The code is ====> " + verificationCode);
        emailService.sendVerificationEmail(email, verificationCode);
    }

    public AuthenticationResponse authenticateWithGoogle(Map<String, String> userData){
        String email = userData.get("email");
        String username = userData.get("username");
        String password = UUID.randomUUID().toString();
        Optional<User> lookupUser = userRepository.findByEmail(userData.get("email"));

        if(lookupUser.isPresent()) {
            User user = lookupUser.get();
            if(user.isWithGoogle()){
                String accessToken = jwtService.generateToken(user);
                String refreshToken = createRefreshToken(user.getId());
                return new AuthenticationResponse(refreshToken, accessToken);
            }

            else{
                throw new AuthenticationException("Invalid email or password!", HttpStatus.UNAUTHORIZED);
            }
        }

        return this.signup(new SignUpRequest(email, password, username), true);
    }

    public String validateVerificationCode(String verificationCode, String userId){
        String redisKey = VERIFICATION_KEY_REDIS + userId;
        String valueFromRedis =  (String) redisTemplate.opsForValue().get(redisKey);

        if(valueFromRedis == null){
            throw new InvalidVerificationException("The verification code has expired!");
        }

        if(!valueFromRedis.equals(verificationCode))
            throw new InvalidVerificationException("The verification code is incorrect!");

        redisTemplate.delete(redisKey);

        User user = userRepository.findById(userId).orElseThrow(() -> new AuthenticationException("User not exists", HttpStatus.UNAUTHORIZED));

        user.setVerified(true);
        user = userRepository.save(user);

        return jwtService.generateToken(user);
    }

    public void resendVerificationCode(String userId, String email){
        String redisKey = VERIFICATION_KEY_REDIS + userId;
        String valueFromRedis = (String) redisTemplate.opsForValue().get(redisKey);

        if(valueFromRedis != null)
            throw new InvalidVerificationException("The verification code has not expired!");

        sendVerificationCode(userId, email);

        //Send an email with the verification code here Or send an event using kafka or webhooks or something else
    }

    public String generate4DigitCode(){
        int code = new Random().nextInt(10000);
        return String.format("%04d", code);
    }
}
