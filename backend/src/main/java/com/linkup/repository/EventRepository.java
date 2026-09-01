package com.linkup.repository;

import com.linkup.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCreatorId(Long creatorId);

    @Query("SELECT e FROM Event e WHERE " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "e.privacy <> 'PRIVATE'")
    List<Event> searchEvents(@Param("query") String query);

    // Global upcoming events
    @Query("SELECT e FROM Event e WHERE e.dateTime > CURRENT_TIMESTAMP ORDER BY e.dateTime ASC")
    List<Event> getUpcomingEvents();

    // User's attending upcoming events
    @Query("SELECT e FROM Event e WHERE e.dateTime > CURRENT_TIMESTAMP AND e.id IN (" +
           "SELECT ea.event.id FROM EventAttendee ea WHERE ea.user.id = :userId AND (ea.status = 'GOING' OR ea.status = 'INTERESTED')" +
           ") ORDER BY e.dateTime ASC")
    List<Event> getUserUpcomingEvents(@Param("userId") Long userId);
}
