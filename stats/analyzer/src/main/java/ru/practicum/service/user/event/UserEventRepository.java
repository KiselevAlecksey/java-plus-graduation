package ru.practicum.service.user.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.service.similarity.event.EventRating;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    Page<UserEvent> findByUserId(Long userId, Pageable pageable);

    Collection<UserEvent> findByEventIdInAndUserId(Collection<Long> eventIds, Long userId);

    Optional<UserEvent> findUserEventByEventIdAndUserId(long eventId, long userId);

    @Query("""
           SELECT 
           ue.eventId as eventId,
           SUM(ue.rating) as rating
           FROM UserEvent ue
           WHERE ue.eventId IN (:eventIds)
           GROUP BY ue.eventId
           ORDER BY SUM(ue.rating) DESC
           """)
    List<EventRating> getTotalWeightsForEvents(@Param("eventIds") List<Long> eventIds);

}
