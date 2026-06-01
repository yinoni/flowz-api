package com.flowzapi.flowz_api_builder.model;

public final class UserBuilder {
    private String id;
    private String username;
    private String password;
    private String email;
    private boolean verified;
    private boolean withGoogle;

    private UserBuilder() {
    }

    public static UserBuilder anUser() {
        return new UserBuilder();
    }

    public UserBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public UserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public UserBuilder withWithGoogle(boolean withGoogle) {
        this.withGoogle = withGoogle;
        return this;
    }

    public User build() {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setVerified(verified);
        user.setWithGoogle(withGoogle);
        return user;
    }
}
