package com.linkup.service;

import com.linkup.model.Notification;
import com.linkup.model.User;
import com.linkup.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Notification createNotification(User recipient, User sender, String type, Long referenceId, String content) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .type(type)
                .referenceId(referenceId)
                .content(content)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Real-time dispatch via WebSocket
        sendRealtimeNotification(saved);

        return saved;
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(userId, false);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    private void sendRealtimeNotification(Notification notification) {
        try {
            String destination = "/topic/notifications/" + notification.getRecipient().getId();
            // A simple DTO payload or map
            var payload = java.util.Map.of(
                "id", notification.getId(),
                "type", notification.getType(),
                "content", notification.getContent(),
                "referenceId", notification.getReferenceId(),
                "isRead", notification.isRead(),
                "createdAt", notification.getCreatedAt().toString(),
                "senderName", notification.getSender() != null 
                    ? notification.getSender().getFirstName() + " " + notification.getSender().getLastName() 
                    : "System",
                "senderAvatar", notification.getSender() != null && notification.getSender().getAvatarUrl() != null
                    ? notification.getSender().getAvatarUrl()
                    : ""
            );
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            // Log and ignore socket failures to preserve HTTP flow resilience
            System.err.println("Failed to dispatch real-time WebSocket notification: " + e.getMessage());
        }
    }
}
