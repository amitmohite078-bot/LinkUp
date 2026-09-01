package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stories")
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(columnDefinition = "TEXT")
    private String textContent;

    private String emoji;

    private String musicTitle;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    private String privacy = "PUBLIC";

    public Story() {
    }

    public Story(Long id, User user, String mediaUrl, String textContent, String emoji, String musicTitle, LocalDateTime expiresAt, LocalDateTime createdAt, String privacy) {
        this.id = id;
        this.user = user;
        this.mediaUrl = mediaUrl;
        this.textContent = textContent;
        this.emoji = emoji;
        this.musicTitle = musicTitle;
        this.expiresAt = expiresAt;
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

    public String getMediaUrl() {
        return this.mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getTextContent() {
        return this.textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getEmoji() {
        return this.emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getMusicTitle() {
        return this.musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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

    public static StoryBuilder builder() {
        return new StoryBuilder();
    }

    public static class StoryBuilder {
        private Long id;
        private User user;
        private String mediaUrl;
        private String textContent;
        private String emoji;
        private String musicTitle;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
        private String privacy = "PUBLIC";

        public StoryBuilder() {}

        public StoryBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public StoryBuilder user(User user) {
            this.user = user;
            return this;
        }

        public StoryBuilder mediaUrl(String mediaUrl) {
            this.mediaUrl = mediaUrl;
            return this;
        }

        public StoryBuilder textContent(String textContent) {
            this.textContent = textContent;
            return this;
        }

        public StoryBuilder emoji(String emoji) {
            this.emoji = emoji;
            return this;
        }

        public StoryBuilder musicTitle(String musicTitle) {
            this.musicTitle = musicTitle;
            return this;
        }

        public StoryBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public StoryBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public StoryBuilder privacy(String privacy) {
            this.privacy = privacy;
            return this;
        }

        public Story build() {
            return new Story(this.id, this.user, this.mediaUrl, this.textContent, this.emoji, this.musicTitle, this.expiresAt, this.createdAt, this.privacy);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = createdAt.plusHours(24);
        }
    }
}
