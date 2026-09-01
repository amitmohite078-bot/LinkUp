package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members", uniqueConstraints = {@UniqueConstraint(columnNames = {"group_id", "user_id"})})
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime joinedAt;

    private String role = "MEMBER";

    private String status = "ACTIVE";

    public GroupMember() {
    }

    public GroupMember(Long id, Group group, User user, LocalDateTime joinedAt, String role, String status) {
        this.id = id;
        this.group = group;
        this.user = user;
        this.joinedAt = joinedAt;
        this.role = role;
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Group getGroup() {
        return this.group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getJoinedAt() {
        return this.joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static GroupMemberBuilder builder() {
        return new GroupMemberBuilder();
    }

    public static class GroupMemberBuilder {
        private Long id;
        private Group group;
        private User user;
        private LocalDateTime joinedAt;
        private String role = "MEMBER";
        private String status = "ACTIVE";

        public GroupMemberBuilder() {}

        public GroupMemberBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GroupMemberBuilder group(Group group) {
            this.group = group;
            return this;
        }

        public GroupMemberBuilder user(User user) {
            this.user = user;
            return this;
        }

        public GroupMemberBuilder joinedAt(LocalDateTime joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public GroupMemberBuilder role(String role) {
            this.role = role;
            return this;
        }

        public GroupMemberBuilder status(String status) {
            this.status = status;
            return this;
        }

        public GroupMember build() {
            return new GroupMember(this.id, this.group, this.user, this.joinedAt, this.role, this.status);
        }
    }
}
