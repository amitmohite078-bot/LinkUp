package com.linkup.repository;

import com.linkup.model.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    List<ChatMember> findByChatRoomId(Long chatRoomId);
    Optional<ChatMember> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
