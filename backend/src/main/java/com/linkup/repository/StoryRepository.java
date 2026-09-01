package com.linkup.repository;

import com.linkup.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    
    // Fetch active unexpired stories for user and their friends
    @Query("SELECT s FROM Story s WHERE s.expiresAt > CURRENT_TIMESTAMP AND (" +
           "s.user.id = :userId OR s.user.id IN (" +
           "SELECT f.addressee.id FROM Friendship f WHERE f.requester.id = :userId AND f.status = 'ACCEPTED' UNION " +
           "SELECT f.requester.id FROM Friendship f WHERE f.addressee.id = :userId AND f.status = 'ACCEPTED'" +
           ")) ORDER BY s.createdAt DESC")
    List<Story> getActiveStories(@Param("userId") Long userId);

    List<Story> findByUserIdOrderByCreatedAtDesc(Long userId);
}
