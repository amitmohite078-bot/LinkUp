package com.linkup.dto;


public class LoginRequest {
    private String emailOrUsername;

    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String emailOrUsername, String password) {
        this.emailOrUsername = emailOrUsername;
        this.password = password;
    }

    public String getEmailOrUsername() {
        return this.emailOrUsername;
    }

    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static LoginRequestBuilder builder() {
        return new LoginRequestBuilder();
    }

    public static class LoginRequestBuilder {
        private String emailOrUsername;
        private String password;

        public LoginRequestBuilder() {}

        public LoginRequestBuilder emailOrUsername(String emailOrUsername) {
            this.emailOrUsername = emailOrUsername;
            return this;
        }

        public LoginRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public LoginRequest build() {
            return new LoginRequest(this.emailOrUsername, this.password);
        }
    }
}
