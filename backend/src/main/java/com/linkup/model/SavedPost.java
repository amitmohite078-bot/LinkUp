package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_posts", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "post_id"})})
public class SavedPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private LocalDateTime savedAt;

    private String categoryName = "Uncategorized";

    public SavedPost() {
    }

    public SavedPost(Long id, User user, Post post, LocalDateTime savedAt, String categoryName) {
        this.id = id;
        this.user = user;
        this.post = post;
        this.savedAt = savedAt;
        this.categoryName = categoryName;
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

    public Post getPost() {
        return this.post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public LocalDateTime getSavedAt() {
        return this.savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public static SavedPostBuilder builder() {
        return new SavedPostBuilder();
    }

    public static class SavedPostBuilder {
        private Long id;
        private User user;
        private Post post;
        private LocalDateTime savedAt;
        private String categoryName = "Uncategorized";

        public SavedPostBuilder() {}

        public SavedPostBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SavedPostBuilder user(User user) {
            this.user = user;
            return this;
        }

        public SavedPostBuilder post(Post post) {
            this.post = post;
            return this;
        }

        public SavedPostBuilder savedAt(LocalDateTime savedAt) {
            this.savedAt = savedAt;
            return this;
        }

        public SavedPostBuilder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public SavedPost build() {
            return new SavedPost(this.id, this.user, this.post, this.savedAt, this.categoryName);
        }
    }
}
