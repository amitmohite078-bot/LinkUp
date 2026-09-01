package com.linkup.repository;

import com.linkup.model.ChatMessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageStatusRepository extends JpaRepository<ChatMessageStatus, Long> {
    List<ChatMessageStatus> findByMessageId(Long messageId);
    Optional<ChatMessageStatus> findByMessageIdAndUserId(Long messageId, Long userId);

    // Count unread messages in a room for a user
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :roomId " +
           "AND m.sender.id <> :userId " +
           "AND m.id NOT IN (" +
           "SELECT cms.message.id FROM ChatMessageStatus cms WHERE cms.user.id = :userId AND cms.status = 'READ'" +
           ")")
    long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
