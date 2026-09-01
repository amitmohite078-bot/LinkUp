package com.linkup.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ChatMessageResponse {
    private Long id;

    private Long roomId;

    private Long senderId;

    private String senderName;

    private String senderAvatar;

    private String content;

    private Long parentMessageId;

    private String parentMessageContent;

    private LocalDateTime createdAt;

    private String type;

    private List<Map<String, String>> reactions;

    private boolean isRead;

    public ChatMessageResponse() {
    }

    public ChatMessageResponse(Long id, Long roomId, Long senderId, String senderName, String senderAvatar, String content, Long parentMessageId, String parentMessageContent, LocalDateTime createdAt, String type, List<Map<String, String>> reactions, boolean isRead) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.content = content;
        this.parentMessageId = parentMessageId;
        this.parentMessageContent = parentMessageContent;
        this.createdAt = createdAt;
        this.type = type;
        this.reactions = reactions;
        this.isRead = isRead;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return this.roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getSenderId() {
        return this.senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return this.senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentMessageId() {
        return this.parentMessageId;
    }

    public void setParentMessageId(Long parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public String getParentMessageContent() {
        return this.parentMessageContent;
    }

    public void setParentMessageContent(String parentMessageContent) {
        this.parentMessageContent = parentMessageContent;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Map<String, String>> getReactions() {
        return this.reactions;
    }

    public void setReactions(List<Map<String, String>> reactions) {
        this.reactions = reactions;
    }

    public boolean isIsRead() {
        return this.isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public static ChatMessageResponseBuilder builder() {
        return new ChatMessageResponseBuilder();
    }

    public static class ChatMessageResponseBuilder {
        private Long id;
        private Long roomId;
        private Long senderId;
        private String senderName;
        private String senderAvatar;
        private String content;
        private Long parentMessageId;
        private String parentMessageContent;
        private LocalDateTime createdAt;
        private String type;
        private List<Map<String, String>> reactions;
        private boolean isRead;

        public ChatMessageResponseBuilder() {}

        public ChatMessageResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChatMessageResponseBuilder roomId(Long roomId) {
            this.roomId = roomId;
            return this;
        }

        public ChatMessageResponseBuilder senderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        public ChatMessageResponseBuilder senderName(String senderName) {
            this.senderName = senderName;
            return this;
        }

        public ChatMessageResponseBuilder senderAvatar(String senderAvatar) {
            this.senderAvatar = senderAvatar;
            return this;
        }

        public ChatMessageResponseBuilder content(String content) {
            this.content = content;
            return this;
        }

        public ChatMessageResponseBuilder parentMessageId(Long parentMessageId) {
            this.parentMessageId = parentMessageId;
            return this;
        }

        public ChatMessageResponseBuilder parentMessageContent(String parentMessageContent) {
            this.parentMessageContent = parentMessageContent;
            return this;
        }

        public ChatMessageResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ChatMessageResponseBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ChatMessageResponseBuilder reactions(List<Map<String, String>> reactions) {
            this.reactions = reactions;
            return this;
        }

        public ChatMessageResponseBuilder isRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public ChatMessageResponse build() {
            return new ChatMessageResponse(this.id, this.roomId, this.senderId, this.senderName, this.senderAvatar, this.content, this.parentMessageId, this.parentMessageContent, this.createdAt, this.type, this.reactions, this.isRead);
        }
    }
}
