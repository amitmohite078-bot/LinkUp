package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocks", uniqueConstraints = {@UniqueConstraint(columnNames = {"blocker_id", "blocked_id"})})
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    private LocalDateTime createdAt;

    public Block() {
    }

    public Block(Long id, User blocker, User blocked, LocalDateTime createdAt) {
        this.id = id;
        this.blocker = blocker;
        this.blocked = blocked;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getBlocker() {
        return this.blocker;
    }

    public void setBlocker(User blocker) {
        this.blocker = blocker;
    }

    public User getBlocked() {
        return this.blocked;
    }

    public void setBlocked(User blocked) {
        this.blocked = blocked;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static BlockBuilder builder() {
        return new BlockBuilder();
    }

    public static class BlockBuilder {
        private Long id;
        private User blocker;
        private User blocked;
        private LocalDateTime createdAt;

        public BlockBuilder() {}

        public BlockBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlockBuilder blocker(User blocker) {
            this.blocker = blocker;
            return this;
        }

        public BlockBuilder blocked(User blocked) {
            this.blocked = blocked;
            return this;
        }

        public BlockBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Block build() {
            return new Block(this.id, this.blocker, this.blocked, this.createdAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
