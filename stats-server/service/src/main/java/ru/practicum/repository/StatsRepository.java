package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.StatDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query(value = "SELECT new ru.practicum.StatDto(" +
            "h.app, h.uri, count(DISTINCT h.ip)) " +
            "FROM Hit as h " +
            "WHERE h.timestamp BETWEEN ?1 AND ?2 " +
            "AND h.uri IN ?3 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY count(DISTINCT h.ip) DESC")
    List<StatDto> getWithUniqueIpCount(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "SELECT new ru.practicum.StatDto(" +
            "h.app, h.uri, count(h.ip)) " +
            "FROM Hit as h " +
            "WHERE h.timestamp BETWEEN ?1 AND ?2 " +
            "AND (?3 IS NULL OR h.uri IN ?3) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY count(h.ip) DESC")
    List<StatDto> getWithIpCount(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "SELECT COUNT(DISTINCT ip) FROM hits WHERE uri = (:uri)", nativeQuery = true)
    Long viewsCount(String uri);
}
