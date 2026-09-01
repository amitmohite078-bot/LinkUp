package com.linkup.model;

import jakarta.persistence.*;

@Entity
@Table(name = "post_reactions", uniqueConstraints = {@UniqueConstraint(columnNames = {"post_id", "user_id"})})
public class PostReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String reactionType;

    public PostReaction() {
    }

    public PostReaction(Long id, Post post, User user, String reactionType) {
        this.id = id;
        this.post = post;
        this.user = user;
        this.reactionType = reactionType;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return this.post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReactionType() {
        return this.reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }

    public static PostReactionBuilder builder() {
        return new PostReactionBuilder();
    }

    public static class PostReactionBuilder {
        private Long id;
        private Post post;
        private User user;
        private String reactionType;

        public PostReactionBuilder() {}

        public PostReactionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PostReactionBuilder post(Post post) {
            this.post = post;
            return this;
        }

        public PostReactionBuilder user(User user) {
            this.user = user;
            return this;
        }

        public PostReactionBuilder reactionType(String reactionType) {
            this.reactionType = reactionType;
            return this;
        }

        public PostReaction build() {
            return new PostReaction(this.id, this.post, this.user, this.reactionType);
        }
    }
}
