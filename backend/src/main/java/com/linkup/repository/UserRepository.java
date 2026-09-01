package com.linkup.repository;

import com.linkup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Fuzzy search for global search bar
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(@Param("query") String query);

    // Friend suggestions (simple implementation: users who are not you, not your friends already, and not blocked)
    @Query("SELECT u FROM User u WHERE u.id <> :userId AND u.id NOT IN (" +
           "SELECT f.addressee.id FROM Friendship f WHERE f.requester.id = :userId UNION " +
           "SELECT f.requester.id FROM Friendship f WHERE f.addressee.id = :userId UNION " +
           "SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :userId UNION " +
           "SELECT b.blocker.id FROM Block b WHERE b.blocked.id = :userId" +
           ")")
    List<User> getFriendSuggestions(@Param("userId") Long userId);
}
