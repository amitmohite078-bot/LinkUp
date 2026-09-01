package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_members", uniqueConstraints = {@UniqueConstraint(columnNames = {"room_id", "user_id"})})
public class ChatMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime joinedAt;

    public ChatMember() {
    }

    public ChatMember(Long id, ChatRoom chatRoom, User user, LocalDateTime joinedAt) {
        this.id = id;
        this.chatRoom = chatRoom;
        this.user = user;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatRoom getChatRoom() {
        return this.chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getJoinedAt() {
        return this.joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public static ChatMemberBuilder builder() {
        return new ChatMemberBuilder();
    }

    public static class ChatMemberBuilder {
        private Long id;
        private ChatRoom chatRoom;
        private User user;
        private LocalDateTime joinedAt;

        public ChatMemberBuilder() {}

        public ChatMemberBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChatMemberBuilder chatRoom(ChatRoom chatRoom) {
            this.chatRoom = chatRoom;
            return this;
        }

        public ChatMemberBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ChatMemberBuilder joinedAt(LocalDateTime joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public ChatMember build() {
            return new ChatMember(this.id, this.chatRoom, this.user, this.joinedAt);
        }
    }
}
