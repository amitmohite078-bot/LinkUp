package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "album_photos")
public class AlbumPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String caption;

    private LocalDateTime createdAt;

    public AlbumPhoto() {
    }

    public AlbumPhoto(Long id, Album album, String photoUrl, String caption, LocalDateTime createdAt) {
        this.id = id;
        this.album = album;
        this.photoUrl = photoUrl;
        this.caption = caption;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Album getAlbum() {
        return this.album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCaption() {
        return this.caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static AlbumPhotoBuilder builder() {
        return new AlbumPhotoBuilder();
    }

    public static class AlbumPhotoBuilder {
        private Long id;
        private Album album;
        private String photoUrl;
        private String caption;
        private LocalDateTime createdAt;

        public AlbumPhotoBuilder() {}

        public AlbumPhotoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AlbumPhotoBuilder album(Album album) {
            this.album = album;
            return this;
        }

        public AlbumPhotoBuilder photoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
            return this;
        }

        public AlbumPhotoBuilder caption(String caption) {
            this.caption = caption;
            return this;
        }

        public AlbumPhotoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AlbumPhoto build() {
            return new AlbumPhoto(this.id, this.album, this.photoUrl, this.caption, this.createdAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
