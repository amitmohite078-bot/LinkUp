package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String firstName;

    private String lastName;

    private String avatarUrl;

    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String relationshipStatus;

    private String work;

    private String education;

    private String location;

    private LocalDateTime joinedAt;

    private String privacyPosts = "PUBLIC";

    private String privacyRequests = "ALL";

    private String privacyFriendsList = "PUBLIC";

    private String privacySearch = "PUBLIC";

    private String privacyMessage = "ALL";

    private String privacyProfile = "PUBLIC";

    public User() {
    }

    public User(Long id, String username, String email, String passwordHash, String firstName, String lastName, String avatarUrl, String coverUrl, String bio, String relationshipStatus, String work, String education, String location, LocalDateTime joinedAt, String privacyPosts, String privacyRequests, String privacyFriendsList, String privacySearch, String privacyMessage, String privacyProfile) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
        this.coverUrl = coverUrl;
        this.bio = bio;
        this.relationshipStatus = relationshipStatus;
        this.work = work;
        this.education = education;
        this.location = location;
        this.joinedAt = joinedAt;
        this.privacyPosts = privacyPosts;
        this.privacyRequests = privacyRequests;
        this.privacyFriendsList = privacyFriendsList;
        this.privacySearch = privacySearch;
        this.privacyMessage = privacyMessage;
        this.privacyProfile = privacyProfile;
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

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public String getRelationshipStatus() {
        return this.relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }

    public String getWork() {
        return this.work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getEducation() {
        return this.education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getJoinedAt() {
        return this.joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getPrivacyPosts() {
        return this.privacyPosts;
    }

    public void setPrivacyPosts(String privacyPosts) {
        this.privacyPosts = privacyPosts;
    }

    public String getPrivacyRequests() {
        return this.privacyRequests;
    }

    public void setPrivacyRequests(String privacyRequests) {
        this.privacyRequests = privacyRequests;
    }

    public String getPrivacyFriendsList() {
        return this.privacyFriendsList;
    }

    public void setPrivacyFriendsList(String privacyFriendsList) {
        this.privacyFriendsList = privacyFriendsList;
    }

    public String getPrivacySearch() {
        return this.privacySearch;
    }

    public void setPrivacySearch(String privacySearch) {
        this.privacySearch = privacySearch;
    }

    public String getPrivacyMessage() {
        return this.privacyMessage;
    }

    public void setPrivacyMessage(String privacyMessage) {
        this.privacyMessage = privacyMessage;
    }

    public String getPrivacyProfile() {
        return this.privacyProfile;
    }

    public void setPrivacyProfile(String privacyProfile) {
        this.privacyProfile = privacyProfile;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String email;
        private String passwordHash;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private String coverUrl;
        private String bio;
        private String relationshipStatus;
        private String work;
        private String education;
        private String location;
        private LocalDateTime joinedAt;
        private String privacyPosts = "PUBLIC";
        private String privacyRequests = "ALL";
        private String privacyFriendsList = "PUBLIC";
        private String privacySearch = "PUBLIC";
        private String privacyMessage = "ALL";
        private String privacyProfile = "PUBLIC";

        public UserBuilder() {}

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public UserBuilder coverUrl(String coverUrl) {
            this.coverUrl = coverUrl;
            return this;
        }

        public UserBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public UserBuilder relationshipStatus(String relationshipStatus) {
            this.relationshipStatus = relationshipStatus;
            return this;
        }

        public UserBuilder work(String work) {
            this.work = work;
            return this;
        }

        public UserBuilder education(String education) {
            this.education = education;
            return this;
        }

        public UserBuilder location(String location) {
            this.location = location;
            return this;
        }

        public UserBuilder joinedAt(LocalDateTime joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public UserBuilder privacyPosts(String privacyPosts) {
            this.privacyPosts = privacyPosts;
            return this;
        }

        public UserBuilder privacyRequests(String privacyRequests) {
            this.privacyRequests = privacyRequests;
            return this;
        }

        public UserBuilder privacyFriendsList(String privacyFriendsList) {
            this.privacyFriendsList = privacyFriendsList;
            return this;
        }

        public UserBuilder privacySearch(String privacySearch) {
            this.privacySearch = privacySearch;
            return this;
        }

        public UserBuilder privacyMessage(String privacyMessage) {
            this.privacyMessage = privacyMessage;
            return this;
        }

        public UserBuilder privacyProfile(String privacyProfile) {
            this.privacyProfile = privacyProfile;
            return this;
        }

        public User build() {
            return new User(this.id, this.username, this.email, this.passwordHash, this.firstName, this.lastName, this.avatarUrl, this.coverUrl, this.bio, this.relationshipStatus, this.work, this.education, this.location, this.joinedAt, this.privacyPosts, this.privacyRequests, this.privacyFriendsList, this.privacySearch, this.privacyMessage, this.privacyProfile);
        }
    }

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
}
