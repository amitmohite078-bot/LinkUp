package com.linkup.repository;

import com.linkup.model.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    List<PostReaction> findByPostId(Long postId);
    Optional<PostReaction> findByPostIdAndUserId(Long postId, Long userId);
    long countByPostIdAndReactionType(Long postId, String reactionType);
    long countByPostId(Long postId);
}
