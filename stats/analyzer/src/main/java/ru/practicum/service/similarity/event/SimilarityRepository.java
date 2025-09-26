package ru.practicum.service.similarity.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {

    @Query("SELECT sc FROM Similarity sc WHERE sc.eventA IN :eventIds OR sc.eventB IN :eventIds")
    List<SimilarCandidate> findByEventAOrEventBIn(@Param("eventIds") List<Long> eventIds);

    @Query("""
        SELECT s 
        FROM Similarity s 
        WHERE (s.eventA IN :eventIds OR s.eventB IN :eventIds) 
        AND NOT (s.eventA IN :excludedEvents OR s.eventB IN :excludedEvents)
        ORDER BY s.similarity DESC
        """)
    List<Similarity> findSimilarEventsForMultipleIds(
            @Param("eventIds") List<Long> eventIds,
            @Param("excludedEvents") List<Long> excludedEvents,
            Pageable pageable
    );

    @Query("""
    SELECT 
        CASE 
            WHEN s.eventA IN :eventIds THEN s.eventB 
            ELSE s.eventA 
        END as event,
        s.similarity as similarity
    FROM Similarity s 
    WHERE (s.eventA IN :eventIds OR s.eventB IN :eventIds) 
    AND NOT (s.eventA IN :excludedEvents OR s.eventB IN :excludedEvents)
    ORDER BY s.similarity DESC
    """)
    List<SimilarCandidate> findSimilarCandidates(
            @Param("eventIds") List<Long> eventIds,
            @Param("excludedEvents") List<Long> excludedEvents,
            Pageable pageable
    );

    /*@Query("""
        SELECT DISTINCT 
            CASE 
                WHEN s.eventA IN :eventIds THEN s.eventB 
                ELSE s.eventA 
            END 
        FROM Similarity s 
        WHERE (s.eventA IN :eventIds OR s.eventB IN :eventIds) 
        AND NOT (s.eventA IN :excludedEvents OR s.eventB IN :excludedEvents)
        ORDER BY s.similarity DESC
        """)
    List<Long> findSimilarCandidates(
            @Param("eventIds") List<Long> eventIds,
            @Param("excludedEvents") List<Long> excludedEvents,
            Pageable pageable
    );*/

    @Query(value = """
    SELECT 
        ranked_neighbors.neighbor, 
        ranked_neighbors.baseEvent, 
        ranked_neighbors.similarity
    FROM (
        SELECT 
            CASE 
                WHEN s.eventA IN :eventIds THEN s.eventB 
                ELSE s.eventA 
            END as neighbor,
            CASE 
                WHEN s.eventA IN :eventIds THEN s.eventA 
                ELSE s.eventB 
            END as baseEvent,
            s.similarity as similarity,
            ROW_NUMBER() OVER (PARTITION BY 
                CASE 
                    WHEN s.eventA IN :eventIds THEN s.eventA 
                    ELSE s.eventB 
                END 
                ORDER BY s.similarity DESC) as rn
        FROM similarity_candidates s 
        WHERE (s.eventA IN :eventIds OR s.eventB IN :eventIds) 
        AND (s.eventA IN :interactionEvents OR s.eventB IN :interactionEvents)
    ) as ranked_neighbors
    WHERE ranked_neighbors.rn <= :k
    ORDER BY ranked_neighbors.baseEvent, ranked_neighbors.similarity DESC
    """, nativeQuery = true)
    List<Object[]> findAllNeighbors(
            @Param("eventIds") List<Long> eventIds,
            @Param("interactionEvents") List<Long> interactionEvents,
            @Param("k") int k
    );



}
