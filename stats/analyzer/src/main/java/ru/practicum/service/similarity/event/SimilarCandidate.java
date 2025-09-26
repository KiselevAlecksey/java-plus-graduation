package ru.practicum.service.similarity.event;

public interface SimilarCandidate {
    Long getEventId();
    Double getSimilarity();
}
