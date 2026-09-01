package com.linkup.dto;


public class AuthResponse {
    private Long id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String avatarUrl;

    private String coverUrl;

    private String bio;

    public AuthResponse() {
    }

    public AuthResponse(Long id, String username, String email, String firstName, String lastName, String avatarUrl, String coverUrl, String bio) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
        this.coverUrl = coverUrl;
        this.bio = bio;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCoverUrl() {
        return this.coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getBio() {
        return this.bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private String coverUrl;
        private String bio;

        public AuthResponseBuilder() {}

        public AuthResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AuthResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AuthResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AuthResponseBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public AuthResponseBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public AuthResponseBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public AuthResponseBuilder coverUrl(String coverUrl) {
            this.coverUrl = coverUrl;
            return this;
        }

        public AuthResponseBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(this.id, this.username, this.email, this.firstName, this.lastName, this.avatarUrl, this.coverUrl, this.bio);
        }
    }
}
