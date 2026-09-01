package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverUrl;

    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private LocalDateTime createdAt;

    private String location;

    private String type = "PHYSICAL";

    private String privacy = "PUBLIC";

    public Event() {
    }

    public Event(Long id, String name, String description, String coverUrl, LocalDateTime dateTime, User creator, LocalDateTime createdAt, String location, String type, String privacy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.coverUrl = coverUrl;
        this.dateTime = dateTime;
        this.creator = creator;
        this.createdAt = createdAt;
        this.location = location;
        this.type = type;
        this.privacy = privacy;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return this.coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public User getCreator() {
        return this.creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrivacy() {
        return this.privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public static class EventBuilder {
        private Long id;
        private String name;
        private String description;
        private String coverUrl;
        private LocalDateTime dateTime;
        private User creator;
        private LocalDateTime createdAt;
        private String location;
        private String type = "PHYSICAL";
        private String privacy = "PUBLIC";

        public EventBuilder() {}

        public EventBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EventBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EventBuilder description(String description) {
            this.description = description;
            return this;
        }

        public EventBuilder coverUrl(String coverUrl) {
            this.coverUrl = coverUrl;
            return this;
        }

        public EventBuilder dateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public EventBuilder creator(User creator) {
            this.creator = creator;
            return this;
        }

        public EventBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public EventBuilder location(String location) {
            this.location = location;
            return this;
        }

        public EventBuilder type(String type) {
            this.type = type;
            return this;
        }

        public EventBuilder privacy(String privacy) {
            this.privacy = privacy;
            return this;
        }

        public Event build() {
            return new Event(this.id, this.name, this.description, this.coverUrl, this.dateTime, this.creator, this.createdAt, this.location, this.type, this.privacy);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
