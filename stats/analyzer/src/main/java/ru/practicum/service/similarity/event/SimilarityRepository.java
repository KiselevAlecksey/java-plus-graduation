package ru.practicum.service.similarity.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {

    @Query("""
           select 
               case when es.eventA != ?1 then es.eventA else es.eventB end as eventId,
               es.similarity as similarity
           from Similarity es
           where (es.eventA = ?1 or es.eventB = ?1)
             and case when es.eventA != ?1 then es.eventA else es.eventB end 
                 not in (select ue.id.eventId from UserEvent ue where ue.id.userId = ?2)
           order by es.similarity desc
           """)
    List<EventRating> getSimilarEvents(Long eventId, Long userId, Pageable pageable);

    @Query("""
           select case when es.eventA not in ?1 then es.eventA else es.eventB end
           from Similarity es
           where (es.eventA in ?1 or es.eventB in ?1)
             and case when es.eventA not in ?1 then es.eventA else es.eventB end 
                 not in (select ue.id.eventId from UserEvent ue where ue.id.userId = ?2)
           group by case when es.eventA not in ?1 then es.eventA else es.eventB end
           order by max(es.similarity) desc
           """)
    List<Long> getSimilarEventsForRecommended(Collection<Long> eventIds, Long userId, Integer maxResults);

    @Query("""
           select 
               case when es.eventA in ?1 then es.eventA else es.eventB end as eventId,
               case when es.eventA not in ?1 then es.eventA else es.eventB end as neighborEventId,
               es.similarity as score
           from Similarity es
           where (es.eventA in ?1 or es.eventB in ?1)
             and case when es.eventA not in ?1 then es.eventA else es.eventB end in 
                 (select ue.id.eventId from UserEvent ue where ue.id.userId = ?2)
           """)
    Collection<NearestNeighborsProjection> getNearestNeighbors(Collection<Long> eventIds, Long userId);
}
