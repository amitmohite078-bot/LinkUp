package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String feelingActivity;

    private String location;

    private LocalDateTime createdAt;

    private String type = "TEXT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_post_id")
    private Post originalPost;

    private String privacy = "PUBLIC";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    public Post() {
    }

    public Post(Long id, User user, String content, String feelingActivity, String location, LocalDateTime createdAt, String type, Post originalPost, String privacy, Group group) {
        this.id = id;
        this.user = user;
        this.content = content;
        this.feelingActivity = feelingActivity;
        this.location = location;
        this.createdAt = createdAt;
        this.type = type;
        this.originalPost = originalPost;
        this.privacy = privacy;
        this.group = group;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFeelingActivity() {
        return this.feelingActivity;
    }

    public void setFeelingActivity(String feelingActivity) {
        this.feelingActivity = feelingActivity;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Post getOriginalPost() {
        return this.originalPost;
    }

    public void setOriginalPost(Post originalPost) {
        this.originalPost = originalPost;
    }

    public String getPrivacy() {
        return this.privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public Group getGroup() {
        return this.group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public static PostBuilder builder() {
        return new PostBuilder();
    }

    public static class PostBuilder {
        private Long id;
        private User user;
        private String content;
        private String feelingActivity;
        private String location;
        private LocalDateTime createdAt;
        private String type = "TEXT";
        private Post originalPost;
        private String privacy = "PUBLIC";
        private Group group;

        public PostBuilder() {}

        public PostBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PostBuilder user(User user) {
            this.user = user;
            return this;
        }

        public PostBuilder content(String content) {
            this.content = content;
            return this;
        }

        public PostBuilder feelingActivity(String feelingActivity) {
            this.feelingActivity = feelingActivity;
            return this;
        }

        public PostBuilder location(String location) {
            this.location = location;
            return this;
        }

        public PostBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PostBuilder type(String type) {
            this.type = type;
            return this;
        }

        public PostBuilder originalPost(Post originalPost) {
            this.originalPost = originalPost;
            return this;
        }

        public PostBuilder privacy(String privacy) {
            this.privacy = privacy;
            return this;
        }

        public PostBuilder group(Group group) {
            this.group = group;
            return this;
        }

        public Post build() {
            return new Post(this.id, this.user, this.content, this.feelingActivity, this.location, this.createdAt, this.type, this.originalPost, this.privacy, this.group);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
