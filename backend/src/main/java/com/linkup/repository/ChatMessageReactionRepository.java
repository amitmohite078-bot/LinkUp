package com.linkup.repository;

import com.linkup.model.ChatMessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageReactionRepository extends JpaRepository<ChatMessageReaction, Long> {
    List<ChatMessageReaction> findByMessageId(Long messageId);
    Optional<ChatMessageReaction> findByMessageIdAndUserId(Long messageId, Long userId);
}
