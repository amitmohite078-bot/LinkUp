package com.linkup.controller;

import com.linkup.dto.PostResponse;
import com.linkup.model.*;
import com.linkup.repository.PostRepository;
import com.linkup.service.GroupService;
import com.linkup.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String privacy,
            @RequestParam(required = false) List<String> rules) {
        try {
            Group group = groupService.createGroup(currentUserId, name, description, privacy, rules);
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long groupId) {
        try {
            GroupMember member = groupService.joinGroup(currentUserId, groupId);
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long groupId) {
        try {
            groupService.leaveGroup(currentUserId, groupId);
            return ResponseEntity.ok("Left group successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/approve/{targetUserId}")
    public ResponseEntity<?> approveMember(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId) {
        try {
            groupService.approveMember(currentUserId, groupId, targetUserId);
            return ResponseEntity.ok("Member request approved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/reject/{targetUserId}")
    public ResponseEntity<?> rejectMember(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId) {
        try {
            groupService.rejectMember(currentUserId, groupId, targetUserId);
            return ResponseEntity.ok("Member request rejected");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/role/{targetUserId}")
    public ResponseEntity<?> changeMemberRole(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId,
            @RequestParam String role) {
        try {
            groupService.changeMemberRole(currentUserId, groupId, targetUserId, role);
            return ResponseEntity.ok("Role updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedGroups(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            List<Group> groups = groupService.getRecommendedGroups(currentUserId);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Group>> searchGroups(@RequestParam String query) {
        return ResponseEntity.ok(groupService.searchGroups(query));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable Long groupId) {
        try {
            Group group = groupService.getById(groupId);
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getGroupMembers(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupMembers(groupId));
    }

    @GetMapping("/{groupId}/rules")
    public ResponseEntity<List<GroupRule>> getGroupRules(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupRules(groupId));
    }

    // Load group feed discussion posts
    @GetMapping("/{groupId}/posts")
    public ResponseEntity<?> getGroupPosts(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long groupId) {
        try {
            // Check privacy/membership: if private, only active members can see posts
            Group group = groupService.getById(groupId);
            if (group.getPrivacy().equals("PRIVATE") && !groupService.isUserActiveMember(currentUserId, groupId)) {
                return ResponseEntity.badRequest().body("Access denied: Private group membership required");
            }

            List<Post> posts = postRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
            List<PostResponse> responses = posts.stream()
                    .map(p -> postService.getPostResponse(p, currentUserId))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
