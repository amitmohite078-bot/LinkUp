package com.linkup.repository;

import com.linkup.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    // Find rooms a user belongs to
    @Query("SELECT r FROM ChatRoom r WHERE r.id IN (" +
           "SELECT cm.chatRoom.id FROM ChatMember cm WHERE cm.user.id = :userId" +
           ") ORDER BY r.createdAt DESC")
    List<ChatRoom> findChatRoomsForUser(@Param("userId") Long userId);

    // Find direct room between two users
    @Query("SELECT r FROM ChatRoom r WHERE r.type = 'ONE_TO_ONE' AND " +
           "r.id IN (SELECT cm1.chatRoom.id FROM ChatMember cm1 WHERE cm1.user.id = :userAId) AND " +
           "r.id IN (SELECT cm2.chatRoom.id FROM ChatMember cm2 WHERE cm2.user.id = :userBId)")
    Optional<ChatRoom> findDirectChatRoomBetween(@Param("userAId") Long userAId, @Param("userBId") Long userBId);
}
