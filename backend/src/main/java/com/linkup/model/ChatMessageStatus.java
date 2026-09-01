package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message_statuses", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"message_id", "user_id"})
})
public class ChatMessageStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime updatedAt;

    private String status = "DELIVERED";

    public ChatMessageStatus() {
    }

    public ChatMessageStatus(Long id, ChatMessage message, User user, LocalDateTime updatedAt, String status) {
        this.id = id;
        this.message = message;
        this.user = user;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatMessage getMessage() {
        return this.message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static ChatMessageStatusBuilder builder() {
        return new ChatMessageStatusBuilder();
    }

    public static class ChatMessageStatusBuilder {
        private Long id;
        private ChatMessage message;
        private User user;
        private LocalDateTime updatedAt;
        private String status = "DELIVERED";

        public ChatMessageStatusBuilder() {}

        public ChatMessageStatusBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChatMessageStatusBuilder message(ChatMessage message) {
            this.message = message;
            return this;
        }

        public ChatMessageStatusBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ChatMessageStatusBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ChatMessageStatusBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ChatMessageStatus build() {
            return new ChatMessageStatus(this.id, this.message, this.user, this.updatedAt, this.status);
        }
    }

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
