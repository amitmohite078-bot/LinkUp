package com.linkup.service;

import com.linkup.model.Story;
import com.linkup.model.StoryViewer;
import com.linkup.model.User;
import com.linkup.repository.StoryRepository;
import com.linkup.repository.StoryViewerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private StoryViewerRepository storyViewerRepository;

    @Autowired
    private UserService userService;

    @Value("${linkup.upload.dir}")
    private String uploadDir;

    @Transactional
    public Story createStory(Long userId, String textContent, String emoji, String musicTitle, 
                             String privacy, MultipartFile file) throws IOException {
        User user = userService.getById(userId);

        String mediaUrl = null;
        if (file != null && !file.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            mediaUrl = "/api/posts/media/" + fileName; // Reuses resource mapping
        }

        Story story = Story.builder()
                .user(user)
                .textContent(textContent)
                .emoji(emoji)
                .musicTitle(musicTitle)
                .privacy(privacy != null ? privacy : "PUBLIC")
                .mediaUrl(mediaUrl)
                .build();

        return storyRepository.save(story);
    }

    public List<Story> getActiveStories(Long userId) {
        return storyRepository.getActiveStories(userId);
    }

    @Transactional
    public void viewStory(Long userId, Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));
        User user = userService.getById(userId);

        if (userService.isBlocked(userId, story.getUser().getId())) {
            throw new RuntimeException("Action restricted: User is blocked");
        }

        // Avoid self-viewer logging
        if (story.getUser().getId().equals(userId)) {
            return;
        }

        if (storyViewerRepository.existsByStoryIdAndUserId(storyId, userId)) {
            return; // Already logged
        }

        StoryViewer viewer = StoryViewer.builder()
                .story(story)
                .user(user)
                .build();
        storyViewerRepository.save(viewer);
    }

    public List<User> getStoryViewers(Long userId, Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        // Only the story creator can view the viewer list
        if (!story.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: You are not the author of this story");
        }

        List<StoryViewer> viewers = storyViewerRepository.findByStoryId(storyId);
        return viewers.stream().map(StoryViewer::getUser).collect(Collectors.toList());
    }

    @Transactional
    public void deleteStory(Long storyId, Long userId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));
        if (!story.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only author can delete story");
        }
        storyRepository.delete(story);
    }
}
