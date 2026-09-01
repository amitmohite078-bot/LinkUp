package com.linkup.repository;

import com.linkup.model.StoryViewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryViewerRepository extends JpaRepository<StoryViewer, Long> {
    List<StoryViewer> findByStoryId(Long storyId);
    long countByStoryId(Long storyId);
    boolean existsByStoryIdAndUserId(Long storyId, Long userId);
}
