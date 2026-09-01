package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_viewers", uniqueConstraints = {@UniqueConstraint(columnNames = {"story_id", "viewer_id"})})
public class StoryViewer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime viewedAt;

    public StoryViewer() {
    }

    public StoryViewer(Long id, Story story, User user, LocalDateTime viewedAt) {
        this.id = id;
        this.story = story;
        this.user = user;
        this.viewedAt = viewedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Story getStory() {
        return this.story;
    }

    public void setStory(Story story) {
        this.story = story;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getViewedAt() {
        return this.viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }

    public static StoryViewerBuilder builder() {
        return new StoryViewerBuilder();
    }

    public static class StoryViewerBuilder {
        private Long id;
        private Story story;
        private User user;
        private LocalDateTime viewedAt;

        public StoryViewerBuilder() {}

        public StoryViewerBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public StoryViewerBuilder story(Story story) {
            this.story = story;
            return this;
        }

        public StoryViewerBuilder user(User user) {
            this.user = user;
            return this;
        }

        public StoryViewerBuilder viewedAt(LocalDateTime viewedAt) {
            this.viewedAt = viewedAt;
            return this;
        }

        public StoryViewer build() {
            return new StoryViewer(this.id, this.story, this.user, this.viewedAt);
        }
    }
}
