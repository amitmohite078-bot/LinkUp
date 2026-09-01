package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime createdAt;

    private String privacy = "PUBLIC";

    public Album() {
    }

    public Album(Long id, User user, String name, String description, LocalDateTime createdAt, String privacy) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.privacy = privacy;
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPrivacy() {
        return this.privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public static AlbumBuilder builder() {
        return new AlbumBuilder();
    }

    public static class AlbumBuilder {
        private Long id;
        private User user;
        private String name;
        private String description;
        private LocalDateTime createdAt;
        private String privacy = "PUBLIC";

        public AlbumBuilder() {}

        public AlbumBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AlbumBuilder user(User user) {
            this.user = user;
            return this;
        }

        public AlbumBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AlbumBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AlbumBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AlbumBuilder privacy(String privacy) {
            this.privacy = privacy;
            return this;
        }

        public Album build() {
            return new Album(this.id, this.user, this.name, this.description, this.createdAt, this.privacy);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
