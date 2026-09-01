package com.linkup.dto;

import java.time.LocalDateTime;

public class ChatRoomResponse {
    private Long id;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    private long unreadCount;

    private String name;

    private String avatarUrl;

    private String type;

    private Long targetUserId;

    public ChatRoomResponse() {
    }

    public ChatRoomResponse(Long id, String lastMessage, LocalDateTime lastMessageTime, long unreadCount, String name, String avatarUrl, String type, Long targetUserId) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.type = type;
        this.targetUserId = targetUserId;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastMessage() {
        return this.lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return this.lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public long getUnreadCount() {
        return this.unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTargetUserId() {
        return this.targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public static ChatRoomResponseBuilder builder() {
        return new ChatRoomResponseBuilder();
    }

    public static class ChatRoomResponseBuilder {
        private Long id;
        private String lastMessage;
        private LocalDateTime lastMessageTime;
        private long unreadCount;
        private String name;
        private String avatarUrl;
        private String type;
        private Long targetUserId;

        public ChatRoomResponseBuilder() {}

        public ChatRoomResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChatRoomResponseBuilder lastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
            return this;
        }

        public ChatRoomResponseBuilder lastMessageTime(LocalDateTime lastMessageTime) {
            this.lastMessageTime = lastMessageTime;
            return this;
        }

        public ChatRoomResponseBuilder unreadCount(long unreadCount) {
            this.unreadCount = unreadCount;
            return this;
        }

        public ChatRoomResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ChatRoomResponseBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public ChatRoomResponseBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ChatRoomResponseBuilder targetUserId(Long targetUserId) {
            this.targetUserId = targetUserId;
            return this;
        }

        public ChatRoomResponse build() {
            return new ChatRoomResponse(this.id, this.lastMessage, this.lastMessageTime, this.unreadCount, this.name, this.avatarUrl, this.type, this.targetUserId);
        }
    }
}
