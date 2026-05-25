package com.flowzapi.flowz_api_builder.model.user;

public final class UserDTOBuilder {
    private String id;
    private String username;
    private String email;

    private UserDTOBuilder() {
    }

    public static UserDTOBuilder anUserDTO() {
        return new UserDTOBuilder();
    }

    public UserDTOBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public UserDTOBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public UserDTOBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserDTO build() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(id);
        userDTO.setUsername(username);
        userDTO.setEmail(email);
        return userDTO;
    }
}
