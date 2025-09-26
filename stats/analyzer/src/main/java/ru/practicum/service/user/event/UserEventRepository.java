package ru.practicum.service.user.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    List<EventRating> findByEventIdIn(List<Long> eventIds);

    @Query("SELECT i.eventId " +
            "FROM UserEvent i " +
            "WHERE i.userId = :userId " +
            "ORDER BY i.timestamp DESC")
    List<Long> findRecentInteractions(
            @Param("userId") Long userId,
            Pageable pageable);

    Optional<UserEvent> findUserEventByEventIdAndUserId(long eventId, long userId);

    List<Long> findEventIdByUserId(long userId);

    @Query("SELECT i.eventId, SUM(i.rating) FROM UserEvent i WHERE i.eventId IN :eventIds GROUP BY i.eventId")
    List<Object[]> getTotalWeightsForEvents(@Param("eventIds") List<Long> eventIds);
}
