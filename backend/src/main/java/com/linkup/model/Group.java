package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private LocalDateTime createdAt;

    private String privacy = "PUBLIC";

    public Group() {
    }

    public Group(Long id, String name, String description, User creator, LocalDateTime createdAt, String privacy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.createdAt = createdAt;
        this.privacy = privacy;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public User getCreator() {
        return this.creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
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

    public static GroupBuilder builder() {
        return new GroupBuilder();
    }

    public static class GroupBuilder {
        private Long id;
        private String name;
        private String description;
        private User creator;
        private LocalDateTime createdAt;
        private String privacy = "PUBLIC";

        public GroupBuilder() {}

        public GroupBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GroupBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GroupBuilder description(String description) {
            this.description = description;
            return this;
        }

        public GroupBuilder creator(User creator) {
            this.creator = creator;
            return this;
        }

        public GroupBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GroupBuilder privacy(String privacy) {
            this.privacy = privacy;
            return this;
        }

        public Group build() {
            return new Group(this.id, this.name, this.description, this.creator, this.createdAt, this.privacy);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
