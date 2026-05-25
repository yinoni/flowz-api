package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.AuthenticationException;
import com.flowzapi.flowz_api_builder.model.User;
import com.flowzapi.flowz_api_builder.model.authentication.AuthenticationRequest;
import com.flowzapi.flowz_api_builder.model.authentication.SignUpRequest;
import com.flowzapi.flowz_api_builder.model.project.ProjectDTO;
import com.flowzapi.flowz_api_builder.model.user.UserDTO;
import com.flowzapi.flowz_api_builder.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.flowzapi.flowz_api_builder.model.UserBuilder.anUser;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AuthenticationException("Username or password incorrect!", HttpStatus.UNAUTHORIZED));
    }

    public UserDTO login(AuthenticationRequest request){
        User lookupUser = this.findByEmail(request.getEmail());


        return lookupUser.convertToUserDTO();

    }

    public UserDTO signup(SignUpRequest request){
        Optional<User> lookupUser = userRepository.findByEmail(request.getEmail());

        if(lookupUser.isPresent())
            throw new AuthenticationException("Email already in use!", HttpStatus.CONFLICT);

        User newUser = anUser()
                .withEmail(request.getEmail())
                .withUsername(request.getUsername())
                .withPassword(request.getPassword())
                .withVerified(false)
                .build();

        newUser = userRepository.save(newUser);

        return newUser.convertToUserDTO();

    }

}
