package com.flowzapi.flowz_api_builder.model;

import com.flowzapi.flowz_api_builder.model.user.UserDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.flowzapi.flowz_api_builder.model.user.UserDTOBuilder.anUserDTO;

@Data
@Document(collection = "projects")
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private boolean verified;

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public UserDTO convertToUserDTO() {
        return anUserDTO()
                .withEmail(this.email)
                .withId(this.id)
                .withUsername(this.username)
                .build();
    }

}
