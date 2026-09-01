package com.linkup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_attendees", uniqueConstraints = {@UniqueConstraint(columnNames = {"event_id", "user_id"})})
public class EventAttendee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime invitedAt;

    private String status = "GOING";

    public EventAttendee() {
    }

    public EventAttendee(Long id, Event event, User user, LocalDateTime invitedAt, String status) {
        this.id = id;
        this.event = event;
        this.user = user;
        this.invitedAt = invitedAt;
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return this.event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getInvitedAt() {
        return this.invitedAt;
    }

    public void setInvitedAt(LocalDateTime invitedAt) {
        this.invitedAt = invitedAt;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static EventAttendeeBuilder builder() {
        return new EventAttendeeBuilder();
    }

    public static class EventAttendeeBuilder {
        private Long id;
        private Event event;
        private User user;
        private LocalDateTime invitedAt;
        private String status = "GOING";

        public EventAttendeeBuilder() {}

        public EventAttendeeBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EventAttendeeBuilder event(Event event) {
            this.event = event;
            return this;
        }

        public EventAttendeeBuilder user(User user) {
            this.user = user;
            return this;
        }

        public EventAttendeeBuilder invitedAt(LocalDateTime invitedAt) {
            this.invitedAt = invitedAt;
            return this;
        }

        public EventAttendeeBuilder status(String status) {
            this.status = status;
            return this;
        }

        public EventAttendee build() {
            return new EventAttendee(this.id, this.event, this.user, this.invitedAt, this.status);
        }
    }
}
