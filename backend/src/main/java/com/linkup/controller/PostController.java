package com.linkup.controller;

import com.linkup.dto.PostResponse;
import com.linkup.model.Comment;
import com.linkup.model.Post;
import com.linkup.model.SavedPost;
import com.linkup.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostService postService;

    @Value("${linkup.upload.dir}")
    private String uploadDir;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String feelingActivity,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String privacy,
            @RequestParam(required = false) Long groupId,
            @RequestPart(required = false) List<MultipartFile> files) {
        try {
            Post post = postService.createPost(currentUserId, content, feelingActivity, location, privacy, groupId, files);
            return ResponseEntity.ok(postService.getPostResponse(post.getId(), currentUserId));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to upload files: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/share")
    public ResponseEntity<?> sharePost(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long postId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String privacy) {
        try {
            Post shared = postService.sharePost(currentUserId, postId, content, privacy);
            return ResponseEntity.ok(postService.getPostResponse(shared.getId(), currentUserId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/react")
    public ResponseEntity<?> reactToPost(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long postId,
            @RequestParam String reactionType) {
        try {
            PostResponse updated = postService.reactToPost(postId, currentUserId, reactionType);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<?> commentOnPost(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long postId,
            @RequestParam(required = false) Long parentCommentId,
            @RequestParam String content,
            @RequestParam(required = false) String mediaUrl) {
        try {
            Comment comment = postService.addComment(postId, currentUserId, parentCommentId, content, mediaUrl);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(value = {"/{postId}/comment", "/{postId}/comments"})
    public ResponseEntity<?> getPostComments(@PathVariable Long postId) {
        try {
            List<Comment> comments = postService.getComments(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            @PathVariable Long postId) {
        try {
            Long userId = currentUserId != null ? currentUserId : 1L;
            PostResponse post = postService.getPostResponse(postId, userId);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/save")
    public ResponseEntity<?> savePost(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long postId,
            @RequestParam(required = false) String categoryName) {
        try {
            SavedPost saved = postService.savePost(currentUserId, postId, categoryName);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/unsave")
    public ResponseEntity<?> unsavePost(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long postId) {
        try {
            postService.unsavePost(currentUserId, postId);
            return ResponseEntity.ok("Post unsaved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/saved")
    public ResponseEntity<?> getSavedPosts(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam(required = false) String category) {
        try {
            List<SavedPost> saved = postService.getSavedPosts(currentUserId, category);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getNewsFeed(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam(required = false) String strategy) {
        try {
            List<PostResponse> feed = postService.getNewsFeed(currentUserId, strategy);
            return ResponseEntity.ok(feed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPosts(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long userId) {
        try {
            List<PostResponse> posts = postService.getUserPosts(userId, currentUserId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam String query) {
        try {
            List<PostResponse> posts = postService.searchPosts(query, currentUserId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/memories")
    public ResponseEntity<?> getMemories(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            List<PostResponse> memories = postService.getMemories(currentUserId);
            return ResponseEntity.ok(memories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long postId) {
        try {
            postService.deletePost(postId, currentUserId);
            return ResponseEntity.ok("Post deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Serve uploaded files
    @GetMapping("/media/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
