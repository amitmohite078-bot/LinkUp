package com.linkup.model;

import jakarta.persistence.*;

@Entity
@Table(name = "post_media")
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mediaUrl;

    private String mediaType;

    public PostMedia() {
    }

    public PostMedia(Long id, Post post, String mediaUrl, String mediaType) {
        this.id = id;
        this.post = post;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
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

    public String getMediaUrl() {
        return this.mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return this.mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public static PostMediaBuilder builder() {
        return new PostMediaBuilder();
    }

    public static class PostMediaBuilder {
        private Long id;
        private Post post;
        private String mediaUrl;
        private String mediaType;

        public PostMediaBuilder() {}

        public PostMediaBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PostMediaBuilder post(Post post) {
            this.post = post;
            return this;
        }

        public PostMediaBuilder mediaUrl(String mediaUrl) {
            this.mediaUrl = mediaUrl;
            return this;
        }

        public PostMediaBuilder mediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public PostMedia build() {
            return new PostMedia(this.id, this.post, this.mediaUrl, this.mediaType);
        }
    }
}
