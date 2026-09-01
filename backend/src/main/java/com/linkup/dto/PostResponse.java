package com.linkup.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PostResponse {
    private Long id;

    private Long userId;

    private String username;

    private String firstName;

    private String lastName;

    private String avatarUrl;

    private String content;

    private String feelingActivity;

    private String location;

    private String privacy;

    private LocalDateTime createdAt;

    private List<String> mediaUrls;

    private Map<String, Long> reactionCounts;

    private long commentCount;

    private long shareCount;

    private String type;

    private String userReaction;

    private PostResponse originalPost;

    public PostResponse() {
    }

    public PostResponse(Long id, Long userId, String username, String firstName, String lastName, String avatarUrl, String content, String feelingActivity, String location, String privacy, LocalDateTime createdAt, List<String> mediaUrls, Map<String, Long> reactionCounts, long commentCount, long shareCount, String type, String userReaction, PostResponse originalPost) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
        this.content = content;
        this.feelingActivity = feelingActivity;
        this.location = location;
        this.privacy = privacy;
        this.createdAt = createdAt;
        this.mediaUrls = mediaUrls;
        this.reactionCounts = reactionCounts;
        this.commentCount = commentCount;
        this.shareCount = shareCount;
        this.type = type;
        this.userReaction = userReaction;
        this.originalPost = originalPost;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFeelingActivity() {
        return this.feelingActivity;
    }

    public void setFeelingActivity(String feelingActivity) {
        this.feelingActivity = feelingActivity;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrivacy() {
        return this.privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getMediaUrls() {
        return this.mediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }

    public Map<String, Long> getReactionCounts() {
        return this.reactionCounts;
    }

    public void setReactionCounts(Map<String, Long> reactionCounts) {
        this.reactionCounts = reactionCounts;
    }

    public long getCommentCount() {
        return this.commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public long getShareCount() {
        return this.shareCount;
    }

    public void setShareCount(long shareCount) {
        this.shareCount = shareCount;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserReaction() {
        return this.userReaction;
    }

    public void setUserReaction(String userReaction) {
        this.userReaction = userReaction;
    }

    public PostResponse getOriginalPost() {
        return this.originalPost;
    }

    public void setOriginalPost(PostResponse originalPost) {
        this.originalPost = originalPost;
    }

    public static PostResponseBuilder builder() {
        return new PostResponseBuilder();
    }

    public static class PostResponseBuilder {
        private Long id;
        private Long userId;
        private String username;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private String content;
        private String feelingActivity;
        private String location;
        private String privacy;
        private LocalDateTime createdAt;
        private List<String> mediaUrls;
        private Map<String, Long> reactionCounts;
        private long commentCount;
        private long shareCount;
        private String type;
        private String userReaction;
        private PostResponse originalPost;

        public PostResponseBuilder() {}

        public PostResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PostResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public PostResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public PostResponseBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public PostResponseBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public PostResponseBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public PostResponseBuilder content(String content) {
            this.content = content;
            return this;
        }

        public PostResponseBuilder feelingActivity(String feelingActivity) {
            this.feelingActivity = feelingActivity;
            return this;
        }

        public PostResponseBuilder location(String location) {
            this.location = location;
            return this;
        }

        public PostResponseBuilder privacy(String privacy) {
            this.privacy = privacy;
            return this;
        }

        public PostResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PostResponseBuilder mediaUrls(List<String> mediaUrls) {
            this.mediaUrls = mediaUrls;
            return this;
        }

        public PostResponseBuilder reactionCounts(Map<String, Long> reactionCounts) {
            this.reactionCounts = reactionCounts;
            return this;
        }

        public PostResponseBuilder commentCount(long commentCount) {
            this.commentCount = commentCount;
            return this;
        }

        public PostResponseBuilder shareCount(long shareCount) {
            this.shareCount = shareCount;
            return this;
        }

        public PostResponseBuilder type(String type) {
            this.type = type;
            return this;
        }

        public PostResponseBuilder userReaction(String userReaction) {
            this.userReaction = userReaction;
            return this;
        }

        public PostResponseBuilder originalPost(PostResponse originalPost) {
            this.originalPost = originalPost;
            return this;
        }

        public PostResponse build() {
            return new PostResponse(this.id, this.userId, this.username, this.firstName, this.lastName, this.avatarUrl, this.content, this.feelingActivity, this.location, this.privacy, this.createdAt, this.mediaUrls, this.reactionCounts, this.commentCount, this.shareCount, this.type, this.userReaction, this.originalPost);
        }
    }
}
