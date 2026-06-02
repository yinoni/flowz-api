package com.flowzapi.flowz_api_builder.model.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class SignUpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long, include at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&)"
    )
    private String password;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    public SignUpRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
