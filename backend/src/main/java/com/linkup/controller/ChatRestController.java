package com.linkup.controller;

import com.linkup.dto.ChatMessageResponse;
import com.linkup.dto.ChatRoomResponse;
import com.linkup.model.ChatRoom;
import com.linkup.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatRestController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/rooms")
    public ResponseEntity<?> getRooms(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            List<ChatRoomResponse> rooms = chatService.getConversationsList(currentUserId);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getHistory(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long roomId) {
        try {
            List<ChatMessageResponse> history = chatService.getRoomHistory(roomId, currentUserId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search/{roomId}")
    public ResponseEntity<?> searchMessages(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long roomId,
            @RequestParam String query) {
        try {
            List<ChatMessageResponse> results = chatService.searchMessages(roomId, query, currentUserId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/read/{roomId}")
    public ResponseEntity<?> markAsRead(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long roomId) {
        try {
            chatService.markAsRead(roomId, currentUserId);
            return ResponseEntity.ok("Messages marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/room/direct")
    public ResponseEntity<?> getOrCreateDirectRoom(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam Long friendId) {
        try {
            ChatRoom room = chatService.createOrGetDirectChatRoom(currentUserId, friendId);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/room/group")
    public ResponseEntity<?> createGroupRoom(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam String name,
            @RequestParam List<Long> memberIds) {
        try {
            ChatRoom room = chatService.createGroupChatRoom(currentUserId, name, memberIds);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = {"/history/{messageId}/react", "/message/{messageId}/react"})
    public ResponseEntity<?> reactToMessage(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long messageId,
            @RequestParam String reactionType) {
        try {
            chatService.reactToMessage(messageId, currentUserId, reactionType);
            return ResponseEntity.ok("Reaction updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
