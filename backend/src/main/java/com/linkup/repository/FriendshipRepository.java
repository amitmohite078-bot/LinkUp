package com.linkup.repository;

import com.linkup.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester.id = :userAId AND f.addressee.id = :userBId) OR " +
           "(f.requester.id = :userBId AND f.addressee.id = :userAId)")
    Optional<Friendship> findFriendshipBetween(@Param("userAId") Long userAId, @Param("userBId") Long userBId);

    // Pending requests received by this user
    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, String status);

    // Sent requests by this user
    List<Friendship> findByRequesterIdAndStatus(Long requesterId, String status);

    // Active friends list query
    @Query("SELECT f FROM Friendship f WHERE f.status = 'ACCEPTED' AND " +
           "(f.requester.id = :userId OR f.addressee.id = :userId)")
    List<Friendship> findActiveFriendships(@Param("userId") Long userId);
}
