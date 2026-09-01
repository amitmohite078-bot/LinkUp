package com.linkup.repository;

import com.linkup.model.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {
    List<EventAttendee> findByEventId(Long eventId);
    List<EventAttendee> findByEventIdAndStatus(Long eventId, String status);
    Optional<EventAttendee> findByEventIdAndUserId(Long eventId, Long userId);
}
