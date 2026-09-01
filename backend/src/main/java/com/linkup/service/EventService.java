package com.linkup.service;

import com.linkup.model.*;
import com.linkup.repository.EventAttendeeRepository;
import com.linkup.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventAttendeeRepository eventAttendeeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Event createEvent(Long creatorId, String name, String description, String location, 
                              String type, String privacy, LocalDateTime dateTime, String coverUrl) {
        User creator = userService.getById(creatorId);

        Event event = Event.builder()
                .name(name)
                .description(description)
                .location(location)
                .type(type != null ? type : "PHYSICAL")
                .privacy(privacy != null ? privacy : "PUBLIC")
                .dateTime(dateTime)
                .coverUrl(coverUrl)
                .creator(creator)
                .build();

        Event saved = eventRepository.save(event);

        // Auto-add creator as GOING
        EventAttendee attendee = EventAttendee.builder()
                .event(saved)
                .user(creator)
                .status("GOING")
                .build();
        eventAttendeeRepository.save(attendee);

        return saved;
    }

    @Transactional
    public EventAttendee rsvpEvent(Long userId, Long eventId, String status) {
        User user = userService.getById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Optional<EventAttendee> existing = eventAttendeeRepository.findByEventIdAndUserId(eventId, userId);
        
        EventAttendee attendee;
        if (existing.isPresent()) {
            attendee = existing.get();
            attendee.setStatus(status.toUpperCase());
        } else {
            attendee = EventAttendee.builder()
                    .event(event)
                    .user(user)
                    .status(status.toUpperCase())
                    .build();
        }

        return eventAttendeeRepository.save(attendee);
    }

    @Transactional
    public void inviteFriendToEvent(Long userId, Long eventId, Long friendId) {
        // Validate friendship
        String status = friendshipService.getFriendshipStatus(userId, friendId);
        if (!status.equals("FRIENDS")) {
            throw new RuntimeException("You can only invite direct friends to events");
        }

        User inviter = userService.getById(userId);
        User friend = userService.getById(friendId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (eventAttendeeRepository.findByEventIdAndUserId(eventId, friendId).isPresent()) {
            return; // Already invited or RSVP'd
        }

        EventAttendee invite = EventAttendee.builder()
                .event(event)
                .user(friend)
                .status("INTERESTED") // Defaults to interested status on invitation
                .build();
        eventAttendeeRepository.save(invite);

        // Notify friend
        String content = inviter.getFirstName() + " " + inviter.getLastName() + " invited you to " + event.getName() + ".";
        notificationService.createNotification(friend, inviter, "EVENT_INVITE", event.getId(), content);
    }

    public List<Event> getUpcomingEvents() {
        return eventRepository.getUpcomingEvents();
    }

    public List<Event> getUserUpcomingEvents(Long userId) {
        return eventRepository.getUserUpcomingEvents(userId);
    }

    public List<Event> searchEvents(String query) {
        return eventRepository.searchEvents(query);
    }

    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public List<EventAttendee> getAttendees(Long eventId) {
        return eventAttendeeRepository.findByEventId(eventId);
    }
}
