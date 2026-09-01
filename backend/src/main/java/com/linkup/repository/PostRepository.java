package com.linkup.repository;

import com.linkup.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Search posts globally
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) AND p.group IS NULL")
    List<Post> searchPosts(@Param("query") String query);

    // Fetch candidate posts for home feed of a given user
    // Candidate posts must not be authored by blocked users
    // Candidates are posts created by:
    // 1. The user themselves
    // 2. Friends of the user (who have accepted friendship)
    // 3. Public posts by anyone
    // 4. Posts in groups the user is a member of
    @Query("SELECT p FROM Post p WHERE p.user.id NOT IN (" +
           "SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :userId UNION " +
           "SELECT b.blocker.id FROM Block b WHERE b.blocked.id = :userId" +
           ") AND (" +
           "p.user.id = :userId OR " +
           "(p.privacy = 'PUBLIC' AND p.group IS NULL) OR " +
           "(p.privacy = 'FRIENDS' AND p.user.id IN (" +
           "SELECT f.addressee.id FROM Friendship f WHERE f.requester.id = :userId AND f.status = 'ACCEPTED' UNION " +
           "SELECT f.requester.id FROM Friendship f WHERE f.addressee.id = :userId AND f.status = 'ACCEPTED'" +
           ")) OR " +
           "(p.group.id IN (" +
           "SELECT gm.group.id FROM GroupMember gm WHERE gm.user.id = :userId AND gm.status = 'ACTIVE'" +
           "))" +
           ")")
    List<Post> getFeedCandidates(@Param("userId") Long userId);

    // Memories lookup: posts by user on month/day of previous years
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId " +
           "AND EXTRACT(MONTH FROM p.createdAt) = :month " +
           "AND EXTRACT(DAY FROM p.createdAt) = :day " +
           "AND EXTRACT(YEAR FROM p.createdAt) < :year " +
           "ORDER BY p.createdAt DESC")
    List<Post> findMemories(@Param("userId") Long userId, 
                            @Param("month") int month, 
                            @Param("day") int day, 
                            @Param("year") int year);

    // Get posts inside a specific group
    List<Post> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}
