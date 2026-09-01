package com.linkup.controller;

import com.linkup.model.Notification;
import com.linkup.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public ResponseEntity<?> getNotifications(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            List<Notification> list = notificationService.getUserNotifications(currentUserId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            long count = notificationService.getUnreadCount(currentUserId);
            return ResponseEntity.ok(java.util.Map.of("unreadCount", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<?> markRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok("Notification marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> readAll(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            notificationService.markAllAsRead(currentUserId);
            return ResponseEntity.ok("All notifications marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
