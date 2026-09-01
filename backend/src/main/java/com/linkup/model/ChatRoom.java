package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    private String name;

    private String type = "ONE_TO_ONE";

    public ChatRoom() {
    }

    public ChatRoom(Long id, LocalDateTime createdAt, String name, String type) {
        this.id = id;
        this.createdAt = createdAt;
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static ChatRoomBuilder builder() {
        return new ChatRoomBuilder();
    }

    public static class ChatRoomBuilder {
        private Long id;
        private LocalDateTime createdAt;
        private String name;
        private String type = "ONE_TO_ONE";

        public ChatRoomBuilder() {}

        public ChatRoomBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChatRoomBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ChatRoomBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ChatRoomBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ChatRoom build() {
            return new ChatRoom(this.id, this.createdAt, this.name, this.type);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
