package com.linkup.repository;

import com.linkup.model.SavedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    List<SavedPost> findByUserIdOrderBySavedAtDesc(Long userId);
    List<SavedPost> findByUserIdAndCategoryNameOrderBySavedAtDesc(Long userId, String categoryName);
    Optional<SavedPost> findByUserIdAndPostId(Long userId, Long postId);
}
