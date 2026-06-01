package com.flowzapi.flowz_api_builder.model;

import com.flowzapi.flowz_api_builder.model.user.UserDTO;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.flowzapi.flowz_api_builder.model.user.UserDTOBuilder.anUserDTO;

@Data
@Document(collection = "projects")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private boolean verified;
    private boolean withGoogle;

    public UserDTO convertToUserDTO() {
        return anUserDTO()
                .withEmail(this.email)
                .withId(this.id)
                .withUsername(this.username)
                .build();
    }

}
