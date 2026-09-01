package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friendships", uniqueConstraints = {@UniqueConstraint(columnNames = {"requester_id", "addressee_id"})})
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    private LocalDateTime createdAt;

    private String status = "PENDING";

    public Friendship() {
    }

    public Friendship(Long id, User requester, User addressee, LocalDateTime createdAt, String status) {
        this.id = id;
        this.requester = requester;
        this.addressee = addressee;
        this.createdAt = createdAt;
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getRequester() {
        return this.requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getAddressee() {
        return this.addressee;
    }

    public void setAddressee(User addressee) {
        this.addressee = addressee;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static FriendshipBuilder builder() {
        return new FriendshipBuilder();
    }

    public static class FriendshipBuilder {
        private Long id;
        private User requester;
        private User addressee;
        private LocalDateTime createdAt;
        private String status = "PENDING";

        public FriendshipBuilder() {}

        public FriendshipBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public FriendshipBuilder requester(User requester) {
            this.requester = requester;
            return this;
        }

        public FriendshipBuilder addressee(User addressee) {
            this.addressee = addressee;
            return this;
        }

        public FriendshipBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FriendshipBuilder status(String status) {
            this.status = status;
            return this;
        }

        public Friendship build() {
            return new Friendship(this.id, this.requester, this.addressee, this.createdAt, this.status);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
