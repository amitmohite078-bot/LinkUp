package com.linkup.controller;

import com.linkup.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketChatController {

    @Autowired
    private ChatService chatService;

    // Incoming path: /app/chat.sendMessage
    @MessageMapping("/chat.sendMessage")
    public void handleSendMessage(SendMessagePayload payload) {
        chatService.sendMessage(
                payload.getRoomId(),
                payload.getSenderId(),
                payload.getContent(),
                payload.getType(),
                payload.getParentMessageId()
        );
    }

    // Incoming path: /app/chat.typing
    @MessageMapping("/chat.typing")
    public void handleTyping(TypingPayload payload) {
        chatService.sendTypingIndicator(
                payload.getRoomId(),
                payload.getUserId(),
                payload.isTyping()
        );
    }

    // Incoming path: /app/chat.online
    @MessageMapping("/chat.online")
    public void handleOnline(OnlinePayload payload) {
        chatService.sendOnlineIndicator(
                payload.getUserId(),
                payload.isOnline()
        );
    }

    public static class SendMessagePayload {
        private Long roomId;
        private Long senderId;
        private String content;
        private String type; // TEXT, IMAGE, FILE
        private Long parentMessageId;

        public SendMessagePayload() {}

        public SendMessagePayload(Long roomId, Long senderId, String content, String type, Long parentMessageId) {
            this.roomId = roomId;
            this.senderId = senderId;
            this.content = content;
            this.type = type;
            this.parentMessageId = parentMessageId;
        }

        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }

        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Long getParentMessageId() { return parentMessageId; }
        public void setParentMessageId(Long parentMessageId) { this.parentMessageId = parentMessageId; }
    }

    public static class TypingPayload {
        private Long roomId;
        private Long userId;
        private boolean isTyping;

        public TypingPayload() {}

        public TypingPayload(Long roomId, Long userId, boolean isTyping) {
            this.roomId = roomId;
            this.userId = userId;
            this.isTyping = isTyping;
        }

        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public boolean isTyping() { return isTyping; }
        public void setTyping(boolean isTyping) { this.isTyping = isTyping; }
    }

    public static class OnlinePayload {
        private Long userId;
        private boolean isOnline;

        public OnlinePayload() {}

        public OnlinePayload(Long userId, boolean isOnline) {
            this.userId = userId;
            this.isOnline = isOnline;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public boolean isOnline() { return isOnline; }
        public void setOnline(boolean isOnline) { this.isOnline = isOnline; }
    }
}
