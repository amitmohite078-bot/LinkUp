package com.linkup.service;

import com.linkup.dto.PostResponse;
import com.linkup.model.*;
import com.linkup.repository.*;
import com.linkup.ranking.FeedRankingEngine;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private PostReactionRepository postReactionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private SavedPostRepository savedPostRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FeedRankingEngine feedRankingEngine;

    @Autowired
    private GroupRepository groupRepository;

    @Value("${linkup.upload.dir}")
    private String uploadDir;

    @Transactional
    public Post createPost(Long userId, String content, String feelingActivity, String location, 
                            String privacy, Long groupId, List<MultipartFile> files) throws IOException {
        User user = userService.getById(userId);
        
        Group group = null;
        if (groupId != null) {
            group = groupRepository.findById(groupId).orElse(null);
        }

        Post post = Post.builder()
                .user(user)
                .content(content)
                .feelingActivity(feelingActivity)
                .location(location)
                .privacy(privacy != null ? privacy : "PUBLIC")
                .group(group)
                .type("TEXT")
                .build();
        
        Post savedPost = postRepository.save(post);

        if (files != null && !files.isEmpty()) {
            List<PostMedia> mediaList = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                String mediaUrl = "/api/posts/media/" + fileName;
                String contentType = file.getContentType();
                String mediaType = (contentType != null && contentType.startsWith("video")) ? "VIDEO" : "IMAGE";
                
                PostMedia media = PostMedia.builder()
                        .post(savedPost)
                        .mediaUrl(mediaUrl)
                        .mediaType(mediaType)
                        .build();
                mediaList.add(media);
            }
            if (!mediaList.isEmpty()) {
                postMediaRepository.saveAll(mediaList);
                // Set post type
                boolean hasVideo = mediaList.stream().anyMatch(m -> m.getMediaType().equals("VIDEO"));
                savedPost.setType(hasVideo ? "VIDEO" : "PHOTO");
                postRepository.save(savedPost);
            }
        }

        return savedPost;
    }

    @Transactional
    public Post sharePost(Long userId, Long originalPostId, String content, String privacy) {
        User user = userService.getById(userId);
        Post original = postRepository.findById(originalPostId)
                .orElseThrow(() -> new RuntimeException("Original post not found"));

        Post shared = Post.builder()
                .user(user)
                .content(content)
                .originalPost(original)
                .privacy(privacy != null ? privacy : "PUBLIC")
                .type("SHARE")
                .build();

        Post saved = postRepository.save(shared);

        // Notify original post author
        if (!original.getUser().getId().equals(userId)) {
            String notificationContent = user.getFirstName() + " " + user.getLastName() + " shared your post.";
            notificationService.createNotification(original.getUser(), user, "POST_SHARE", saved.getId(), notificationContent);
        }

        return saved;
    }

    @Transactional
    public PostResponse reactToPost(Long postId, Long userId, String reactionType) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userService.getById(userId);

        Optional<PostReaction> existingOpt = postReactionRepository.findByPostIdAndUserId(postId, userId);

        if (existingOpt.isPresent()) {
            PostReaction existing = existingOpt.get();
            if (existing.getReactionType().equalsIgnoreCase(reactionType)) {
                // If clicking same reaction, remove it (unlike)
                postReactionRepository.delete(existing);
            } else {
                // If different, update reaction type
                existing.setReactionType(reactionType.toUpperCase());
                postReactionRepository.save(existing);
            }
        } else {
            // New reaction
            PostReaction reaction = PostReaction.builder()
                    .post(post)
                    .user(user)
                    .reactionType(reactionType.toUpperCase())
                    .build();
            postReactionRepository.save(reaction);

            // Notify post author
            if (!post.getUser().getId().equals(userId)) {
                String notificationContent = user.getFirstName() + " " + user.getLastName() + " reacted with " + reactionType + " to your post.";
                notificationService.createNotification(post.getUser(), user, "POST_REACTION", post.getId(), notificationContent);
            }
        }

        return getPostResponse(postId, userId);
    }

    @Transactional
    public Comment addComment(Long postId, Long userId, Long parentCommentId, String content, String mediaUrl) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userService.getById(userId);

        Comment parent = null;
        if (parentCommentId != null) {
            parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .parentComment(parent)
                .content(content)
                .mediaUrl(mediaUrl)
                .build();

        Comment saved = commentRepository.save(comment);

        // Notify post author
        if (!post.getUser().getId().equals(userId)) {
            String notificationContent = user.getFirstName() + " " + user.getLastName() + " commented on your post.";
            notificationService.createNotification(post.getUser(), user, "POST_COMMENT", post.getId(), notificationContent);
        }

        // Notify parent comment author if reply
        if (parent != null && !parent.getUser().getId().equals(userId)) {
            String notificationContent = user.getFirstName() + " " + user.getLastName() + " replied to your comment.";
            notificationService.createNotification(parent.getUser(), user, "COMMENT_REPLY", post.getId(), notificationContent);
        }

        return saved;
    }

    public List<Comment> getComments(Long postId) {
        return commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId);
    }

    @Transactional
    public SavedPost savePost(Long userId, Long postId, String categoryName) {
        User user = userService.getById(userId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<SavedPost> existing = savedPostRepository.findByUserIdAndPostId(userId, postId);
        if (existing.isPresent()) {
            SavedPost sp = existing.get();
            if (categoryName != null) {
                sp.setCategoryName(categoryName);
                return savedPostRepository.save(sp);
            }
            return sp;
        }

        SavedPost sp = SavedPost.builder()
                .user(user)
                .post(post)
                .categoryName(categoryName != null ? categoryName : "Uncategorized")
                .build();
        return savedPostRepository.save(sp);
    }

    @Transactional
    public void unsavePost(Long userId, Long postId) {
        savedPostRepository.findByUserIdAndPostId(userId, postId).ifPresent(sp -> {
            savedPostRepository.delete(sp);
        });
    }

    public List<SavedPost> getSavedPosts(Long userId, String category) {
        if (category == null || category.trim().isEmpty()) {
            return savedPostRepository.findByUserIdOrderBySavedAtDesc(userId);
        }
        return savedPostRepository.findByUserIdAndCategoryNameOrderBySavedAtDesc(userId, category);
    }

    public List<PostResponse> getNewsFeed(Long userId, String strategy) {
        // Fetch candidates
        List<Post> candidates = postRepository.getFeedCandidates(userId);
        
        // Map to PostResponse DTOs
        List<PostResponse> responses = candidates.stream()
                .map(p -> getPostResponse(p, userId))
                .collect(Collectors.toList());

        // Sort via Feed Ranking Engine
        return feedRankingEngine.rank(responses, userId, strategy);
    }

    public List<PostResponse> getUserPosts(Long targetUserId, Long currentUserId) {
        // Exclude blocked users
        if (userService.isBlocked(currentUserId, targetUserId)) {
            return Collections.emptyList();
        }

        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(targetUserId);
        
        // Filter by privacy checks
        String friendshipStatus = friendshipService.getFriendshipStatus(currentUserId, targetUserId);
        boolean isFriend = friendshipStatus.equals("FRIENDS") || targetUserId.equals(currentUserId);
        
        return posts.stream()
                .filter(p -> p.getGroup() == null) // don't return group posts directly on standard user profile posts query
                .filter(p -> {
                    if (p.getPrivacy().equals("PUBLIC")) return true;
                    if (p.getPrivacy().equals("FRIENDS") && isFriend) return true;
                    return p.getUser().getId().equals(currentUserId);
                })
                .map(p -> getPostResponse(p, currentUserId))
                .collect(Collectors.toList());
    }

    public List<PostResponse> searchPosts(String query, Long userId) {
        List<Post> posts = postRepository.searchPosts(query);
        return posts.stream()
                .filter(p -> !userService.isBlocked(userId, p.getUser().getId()))
                .map(p -> getPostResponse(p, userId))
                .collect(Collectors.toList());
    }

    public List<PostResponse> getMemories(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Post> posts = postRepository.findMemories(userId, now.getMonthValue(), now.getDayOfMonth(), now.getYear());
        return posts.stream()
                .map(p -> getPostResponse(p, userId))
                .collect(Collectors.toList());
    }

    public PostResponse getPostResponse(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return getPostResponse(post, currentUserId);
    }

    public PostResponse getPostResponse(Post post, Long currentUserId) {
        List<PostMedia> media = postMediaRepository.findByPostId(post.getId());
        List<String> mediaUrls = media.stream().map(PostMedia::getMediaUrl).collect(Collectors.toList());

        List<PostReaction> reactions = postReactionRepository.findByPostId(post.getId());
        Map<String, Long> reactionCounts = reactions.stream()
                .collect(Collectors.groupingBy(PostReaction::getReactionType, Collectors.counting()));

        String userReaction = reactions.stream()
                .filter(r -> r.getUser().getId().equals(currentUserId))
                .map(PostReaction::getReactionType)
                .findFirst()
                .orElse(null);

        long commentsCount = commentRepository.countByPostId(post.getId());

        PostResponse originalPostResponse = null;
        if (post.getOriginalPost() != null) {
            originalPostResponse = getPostResponse(post.getOriginalPost(), currentUserId);
        }

        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .firstName(post.getUser().getFirstName())
                .lastName(post.getUser().getLastName())
                .avatarUrl(post.getUser().getAvatarUrl())
                .content(post.getContent())
                .type(post.getType())
                .feelingActivity(post.getFeelingActivity())
                .location(post.getLocation())
                .privacy(post.getPrivacy())
                .createdAt(post.getCreatedAt())
                .mediaUrls(mediaUrls)
                .reactionCounts(reactionCounts)
                .userReaction(userReaction)
                .commentCount(commentsCount)
                .shareCount(0) // Can count references in DB if needed
                .originalPost(originalPostResponse)
                .build();
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only author can delete post");
        }
        
        postMediaRepository.deleteByPostId(postId);
        // Cascades can also handle replies, reactions, etc., or delete them here
        postRepository.delete(post);
    }
}
