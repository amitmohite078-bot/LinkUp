package com.linkup.repository;

import com.linkup.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    // Fuzzy search for messages in a chat room
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :roomId AND " +
           "LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY m.createdAt DESC")
    List<ChatMessage> searchMessagesInRoom(@Param("roomId") Long roomId, @Param("query") String query);

    // Get the last message in a chat room
    ChatMessage findFirstByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);
}
