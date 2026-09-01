package com.linkup.controller;

import com.linkup.model.User;
import com.linkup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            User user = userService.getById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String relationshipStatus,
            @RequestParam(required = false) String work,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false) String coverUrl) {
        try {
            User updated = userService.updateProfile(currentUserId, firstName, lastName, bio, relationshipStatus, work, education, location, avatarUrl, coverUrl);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/privacy")
    public ResponseEntity<?> updatePrivacy(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam(required = false) String privacyPosts,
            @RequestParam(required = false) String privacyRequests,
            @RequestParam(required = false) String privacyFriendsList,
            @RequestParam(required = false) String privacySearch,
            @RequestParam(required = false) String privacyMessage,
            @RequestParam(required = false) String privacyProfile) {
        try {
            User updated = userService.updatePrivacy(currentUserId, privacyPosts, privacyRequests, privacyFriendsList, privacySearch, privacyMessage, privacyProfile);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> getSuggestions(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            List<User> suggestions = userService.getFriendSuggestions(currentUserId);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{targetId}/block")
    public ResponseEntity<?> blockUser(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long targetId) {
        try {
            userService.blockUser(currentUserId, targetId);
            return ResponseEntity.ok().body("User blocked successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{targetId}/unblock")
    public ResponseEntity<?> unblockUser(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long targetId) {
        try {
            userService.unblockUser(currentUserId, targetId);
            return ResponseEntity.ok().body("User unblocked successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
