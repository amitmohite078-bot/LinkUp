package com.linkup.controller;

import com.linkup.model.Friendship;
import com.linkup.model.User;
import com.linkup.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    @PostMapping("/request/send/{friendId}")
    public ResponseEntity<?> sendRequest(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long friendId) {
        try {
            Friendship request = friendshipService.sendFriendRequest(currentUserId, friendId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/request/accept/{friendId}")
    public ResponseEntity<?> acceptRequest(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long friendId) {
        try {
            Friendship active = friendshipService.acceptFriendRequest(currentUserId, friendId);
            return ResponseEntity.ok(active);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/request/delete/{friendId}")
    public ResponseEntity<?> deleteRequest(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long friendId) {
        try {
            friendshipService.deleteFriendRequest(currentUserId, friendId);
            return ResponseEntity.ok("Friend request deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/remove/{friendId}")
    public ResponseEntity<?> removeFriend(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long friendId) {
        try {
            friendshipService.removeFriend(currentUserId, friendId);
            return ResponseEntity.ok("Friend removed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<List<User>> getFriendsList(@PathVariable Long userId) {
        return ResponseEntity.ok(friendshipService.getFriendsList(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            return ResponseEntity.ok(friendshipService.getPendingRequests(currentUserId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getSentRequests(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            return ResponseEntity.ok(friendshipService.getSentRequests(currentUserId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mutual/{friendId}")
    public ResponseEntity<?> getMutualFriends(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long friendId) {
        try {
            List<User> mutual = friendshipService.getMutualFriends(currentUserId, friendId);
            return ResponseEntity.ok(mutual);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/status/{friendId}")
    public ResponseEntity<?> getFriendshipStatus(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long friendId) {
        try {
            String status = friendshipService.getFriendshipStatus(currentUserId, friendId);
            return ResponseEntity.ok().body(java.util.Map.of("status", status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
