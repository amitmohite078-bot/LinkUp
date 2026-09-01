package com.linkup.controller;

import com.linkup.model.Event;
import com.linkup.model.EventAttendee;
import com.linkup.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String privacy,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam(required = false) String coverUrl) {
        try {
            Event event = eventService.createEvent(currentUserId, name, description, location, type, privacy, dateTime, coverUrl);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{eventId}/rsvp")
    public ResponseEntity<?> rsvpEvent(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long eventId,
            @RequestParam String status) {
        try {
            EventAttendee attendee = eventService.rsvpEvent(currentUserId, eventId, status);
            return ResponseEntity.ok(attendee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{eventId}/invite/{friendId}")
    public ResponseEntity<?> inviteFriend(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long eventId,
            @PathVariable Long friendId) {
        try {
            eventService.inviteFriendToEvent(currentUserId, eventId, friendId);
            return ResponseEntity.ok("Friend invited successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents() {
        return ResponseEntity.ok(eventService.getUpcomingEvents());
    }

    @GetMapping("/user-upcoming")
    public ResponseEntity<?> getUserUpcomingEvents(@RequestHeader("X-User-Id") Long currentUserId) {
        try {
            List<Event> events = eventService.getUserUpcomingEvents(currentUserId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String query) {
        return ResponseEntity.ok(eventService.searchEvents(query));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEvent(@PathVariable Long eventId) {
        try {
            Event event = eventService.getById(eventId);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{eventId}/attendees")
    public ResponseEntity<List<EventAttendee>> getAttendees(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getAttendees(eventId));
    }
}
