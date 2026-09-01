package com.linkup.model;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_message_reactions", uniqueConstraints = {@UniqueConstraint(columnNames = {"message_id", "user_id"})})
public class ChatMessageReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String reactionType;

    public ChatMessageReaction() {
    }

    public ChatMessageReaction(Long id, ChatMessage message, User user, String reactionType) {
        this.id = id;
        this.message = message;
        this.user = user;
        this.reactionType = reactionType;
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

    public String getReactionType() {
        return this.reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }

    public static ChatMessageReactionBuilder builder() {
        return new ChatMessageReactionBuilder();
    }

    public static class ChatMessageReactionBuilder {
        private Long id;
        private ChatMessage message;
        private User user;
        private String reactionType;

        public ChatMessageReactionBuilder() {}

        public ChatMessageReactionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChatMessageReactionBuilder message(ChatMessage message) {
            this.message = message;
            return this;
        }

        public ChatMessageReactionBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ChatMessageReactionBuilder reactionType(String reactionType) {
            this.reactionType = reactionType;
            return this;
        }

        public ChatMessageReaction build() {
            return new ChatMessageReaction(this.id, this.message, this.user, this.reactionType);
        }
    }
}
