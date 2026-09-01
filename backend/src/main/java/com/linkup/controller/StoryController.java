package com.linkup.controller;

import com.linkup.model.Story;
import com.linkup.model.User;
import com.linkup.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/stories")
@CrossOrigin(origins = "*")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createStory(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam(required = false) String textContent,
            @RequestParam(required = false) String emoji,
            @RequestParam(required = false) String musicTitle,
            @RequestParam(required = false) String privacy,
            @RequestPart(required = false) MultipartFile file) {
        try {
            Story story = storyService.createStory(currentUserId, textContent, emoji, musicTitle, privacy, file);
            return ResponseEntity.ok(story);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to upload story media: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveStories(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            List<Story> stories = storyService.getActiveStories(currentUserId);
            return ResponseEntity.ok(stories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{storyId}/view")
    public ResponseEntity<?> viewStory(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long storyId) {
        try {
            storyService.viewStory(currentUserId, storyId);
            return ResponseEntity.ok("View registered");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{storyId}/viewers")
    public ResponseEntity<?> getViewers(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long storyId) {
        try {
            List<User> viewers = storyService.getStoryViewers(currentUserId, storyId);
            return ResponseEntity.ok(viewers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<?> deleteStory(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long storyId) {
        try {
            storyService.deleteStory(storyId, currentUserId);
            return ResponseEntity.ok("Story deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
