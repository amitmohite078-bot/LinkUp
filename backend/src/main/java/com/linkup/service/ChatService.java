package com.linkup.service;

import com.linkup.dto.ChatMessageResponse;
import com.linkup.dto.ChatRoomResponse;
import com.linkup.model.*;
import com.linkup.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageReactionRepository chatMessageReactionRepository;

    @Autowired
    private ChatMessageStatusRepository chatMessageStatusRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatRoom createOrGetDirectChatRoom(Long userAId, Long userBId) {
        if (userService.isBlocked(userAId, userBId)) {
            throw new RuntimeException("Action restricted: User is blocked");
        }

        Optional<ChatRoom> existing = chatRoomRepository.findDirectChatRoomBetween(userAId, userBId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new direct room
        ChatRoom room = ChatRoom.builder()
                .type("ONE_TO_ONE")
                .build();
        ChatRoom savedRoom = chatRoomRepository.save(room);

        User userA = userService.getById(userAId);
        User userB = userService.getById(userBId);

        ChatMember memberA = ChatMember.builder().chatRoom(savedRoom).user(userA).build();
        ChatMember memberB = ChatMember.builder().chatRoom(savedRoom).user(userB).build();
        chatMemberRepository.saveAll(List.of(memberA, memberB));

        return savedRoom;
    }

    @Transactional
    public ChatRoom createGroupChatRoom(Long creatorId, String name, List<Long> memberIds) {
        ChatRoom room = ChatRoom.builder()
                .name(name)
                .type("GROUP")
                .build();
        ChatRoom savedRoom = chatRoomRepository.save(room);

        User creator = userService.getById(creatorId);
        ChatMember creatorMember = ChatMember.builder().chatRoom(savedRoom).user(creator).build();
        chatMemberRepository.save(creatorMember);

        if (memberIds != null) {
            List<ChatMember> members = memberIds.stream()
                    .filter(id -> !id.equals(creatorId))
                    .map(id -> ChatMember.builder().chatRoom(savedRoom).user(userService.getById(id)).build())
                    .collect(Collectors.toList());
            chatMemberRepository.saveAll(members);

            // Notify members
            for (Long id : memberIds) {
                if (id.equals(creatorId)) continue;
                String content = creator.getFirstName() + " added you to the group chat \"" + name + "\".";
                notificationService.createNotification(userService.getById(id), creator, "GROUP_CHAT_ADD", savedRoom.getId(), content);
            }
        }

        return savedRoom;
    }

    @Transactional
    public ChatMessage sendMessage(Long roomId, Long senderId, String content, String type, Long parentMessageId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        User sender = userService.getById(senderId);

        if (!chatMemberRepository.existsByChatRoomIdAndUserId(roomId, senderId)) {
            throw new RuntimeException("Unauthorized: You are not a member of this chat room");
        }

        // Check blocking (only block direct sending, group chat bypasses or operates under separate rules)
        if (room.getType().equals("ONE_TO_ONE")) {
            List<ChatMember> members = chatMemberRepository.findByChatRoomId(roomId);
            Long recipientId = members.stream()
                    .map(m -> m.getUser().getId())
                    .filter(id -> !id.equals(senderId))
                    .findFirst()
                    .orElse(null);
            if (recipientId != null && userService.isBlocked(senderId, recipientId)) {
                throw new RuntimeException("Action restricted: User is blocked");
            }
        }

        ChatMessage parent = null;
        if (parentMessageId != null) {
            parent = chatMessageRepository.findById(parentMessageId)
                    .orElseThrow(() -> new RuntimeException("Parent message not found"));
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(content)
                .type(type != null ? type : "TEXT")
                .parentMessage(parent)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // Auto-deliver to other members in the database (defaults status = DELIVERED)
        List<ChatMember> members = chatMemberRepository.findByChatRoomId(roomId);
        List<ChatMessageStatus> statuses = members.stream()
                .filter(m -> !m.getUser().getId().equals(senderId))
                .map(m -> ChatMessageStatus.builder()
                        .message(saved)
                        .user(m.getUser())
                        .status("DELIVERED")
                        .build())
                .collect(Collectors.toList());
        chatMessageStatusRepository.saveAll(statuses);

        // Convert to response DTO and broadcast
        ChatMessageResponse response = getMessageResponse(saved, senderId);
        broadcastMessage(roomId, response);

        return saved;
    }

    @Transactional
    public void markAsRead(Long roomId, Long userId) {
        List<ChatMember> members = chatMemberRepository.findByChatRoomId(roomId);
        boolean isMember = members.stream().anyMatch(m -> m.getUser().getId().equals(userId));
        if (!isMember) return;

        // Fetch all statuses in this room for this user where status = DELIVERED, and mark as READ
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
        List<ChatMessageStatus> statusesToUpdate = new ArrayList<>();
        
        for (ChatMessage m : messages) {
            if (m.getSender().getId().equals(userId)) continue;
            Optional<ChatMessageStatus> statusOpt = chatMessageStatusRepository.findByMessageIdAndUserId(m.getId(), userId);
            if (statusOpt.isPresent() && statusOpt.get().getStatus().equals("DELIVERED")) {
                ChatMessageStatus s = statusOpt.get();
                s.setStatus("READ");
                statusesToUpdate.add(s);
            }
        }

        if (!statusesToUpdate.isEmpty()) {
            chatMessageStatusRepository.saveAll(statusesToUpdate);
            // Broadcast read receipts update
            messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/read", Map.of("readerId", userId));
        }
    }

    @Transactional
    public void reactToMessage(Long messageId, Long userId, String reactionType) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        User user = userService.getById(userId);

        if (!chatMemberRepository.existsByChatRoomIdAndUserId(message.getChatRoom().getId(), userId)) {
            throw new RuntimeException("Access denied");
        }

        Optional<ChatMessageReaction> existingOpt = chatMessageReactionRepository.findByMessageIdAndUserId(messageId, userId);
        if (existingOpt.isPresent()) {
            ChatMessageReaction existing = existingOpt.get();
            if (existing.getReactionType().equalsIgnoreCase(reactionType)) {
                chatMessageReactionRepository.delete(existing);
            } else {
                existing.setReactionType(reactionType.toUpperCase());
                chatMessageReactionRepository.save(existing);
            }
        } else {
            ChatMessageReaction reaction = ChatMessageReaction.builder()
                    .message(message)
                    .user(user)
                    .reactionType(reactionType.toUpperCase())
                    .build();
            chatMessageReactionRepository.save(reaction);
        }

        // Broadcast reaction update
        List<ChatMessageReaction> reactions = chatMessageReactionRepository.findByMessageId(messageId);
        List<Map<String, String>> mappedReactions = reactions.stream()
                .map(r -> Map.of("username", r.getUser().getUsername(), "reaction", r.getReactionType()))
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatRoom().getId() + "/reactions", Map.of(
                "messageId", messageId,
                "reactions", mappedReactions
        ));
    }

    public List<ChatRoomResponse> getConversationsList(Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findChatRoomsForUser(userId);
        
        return rooms.stream().map(room -> {
            String name = room.getName();
            String avatarUrl = "";
            Long targetUserId = null;

            if (room.getType().equals("ONE_TO_ONE")) {
                List<ChatMember> members = chatMemberRepository.findByChatRoomId(room.getId());
                User friend = members.stream()
                        .map(ChatMember::getUser)
                        .filter(u -> !u.getId().equals(userId))
                        .findFirst()
                        .orElse(null);
                if (friend != null) {
                    name = friend.getFirstName() + " " + friend.getLastName();
                    avatarUrl = friend.getAvatarUrl();
                    targetUserId = friend.getId();
                } else {
                    name = "Deleted User";
                }
            }

            ChatMessage lastMsg = chatMessageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(room.getId());
            String lastContent = lastMsg != null ? lastMsg.getContent() : "No messages yet";
            LocalDateTime lastTime = lastMsg != null ? lastMsg.getCreatedAt() : room.getCreatedAt();

            long unread = chatMessageStatusRepository.countUnreadMessages(room.getId(), userId);

            return ChatRoomResponse.builder()
                    .id(room.getId())
                    .name(name)
                    .avatarUrl(avatarUrl)
                    .type(room.getType())
                    .targetUserId(targetUserId)
                    .lastMessage(lastContent)
                    .lastMessageTime(lastTime)
                    .unreadCount(unread)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<ChatMessageResponse> getRoomHistory(Long roomId, Long userId) {
        if (!chatMemberRepository.existsByChatRoomIdAndUserId(roomId, userId)) {
            throw new RuntimeException("Unauthorized: Access denied");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
        return messages.stream()
                .map(m -> getMessageResponse(m, userId))
                .collect(Collectors.toList());
    }

    public List<ChatMessageResponse> searchMessages(Long roomId, String query, Long userId) {
        if (!chatMemberRepository.existsByChatRoomIdAndUserId(roomId, userId)) {
            throw new RuntimeException("Unauthorized: Access denied");
        }
        List<ChatMessage> messages = chatMessageRepository.searchMessagesInRoom(roomId, query);
        return messages.stream()
                .map(m -> getMessageResponse(m, userId))
                .collect(Collectors.toList());
    }

    public void sendTypingIndicator(Long roomId, Long userId, boolean isTyping) {
        User user = userService.getById(userId);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/typing", Map.of(
                "userId", userId,
                "username", user.getUsername(),
                "isTyping", isTyping
        ));
    }

    public void sendOnlineIndicator(Long userId, boolean isOnline) {
        messagingTemplate.convertAndSend("/topic/online", Map.of(
                "userId", userId,
                "isOnline", isOnline
        ));
    }

    private ChatMessageResponse getMessageResponse(ChatMessage m, Long currentUserId) {
        List<ChatMessageReaction> reactions = chatMessageReactionRepository.findByMessageId(m.getId());
        List<Map<String, String>> mappedReactions = reactions.stream()
                .map(r -> Map.of("username", r.getUser().getUsername(), "reaction", r.getReactionType()))
                .collect(Collectors.toList());

        // Check if message is read (meaning all recipients have read it, or just if target has read it)
        // For simplicity: if ONE_TO_ONE, it is read if the other user's status is READ.
        boolean isRead = false;
        if (m.getChatRoom().getType().equals("ONE_TO_ONE")) {
            List<ChatMessageStatus> statuses = chatMessageStatusRepository.findByMessageId(m.getId());
            isRead = statuses.stream()
                    .filter(s -> !s.getUser().getId().equals(m.getSender().getId()))
                    .anyMatch(s -> s.getStatus().equals("READ"));
        }

        return ChatMessageResponse.builder()
                .id(m.getId())
                .roomId(m.getChatRoom().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFirstName() + " " + m.getSender().getLastName())
                .senderAvatar(m.getSender().getAvatarUrl())
                .content(m.getContent())
                .type(m.getType())
                .parentMessageId(m.getParentMessage() != null ? m.getParentMessage().getId() : null)
                .parentMessageContent(m.getParentMessage() != null ? m.getParentMessage().getContent() : null)
                .createdAt(m.getCreatedAt())
                .reactions(mappedReactions)
                .isRead(isRead)
                .build();
    }

    private void broadcastMessage(Long roomId, ChatMessageResponse response) {
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
    }
}
