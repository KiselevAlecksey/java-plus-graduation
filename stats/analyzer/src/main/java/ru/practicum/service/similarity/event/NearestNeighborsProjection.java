package ru.practicum.service.similarity.event;

public interface NearestNeighborsProjection {
    Long getEventId();
    Long getNeighborEventId();
    Double getScore();
}