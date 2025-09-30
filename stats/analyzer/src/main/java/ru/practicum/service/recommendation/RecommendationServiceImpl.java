package ru.practicum.service.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.service.similarity.event.NearestNeighborsProjection;
import ru.practicum.service.similarity.event.SimilarityRepository;
import ru.practicum.service.similarity.event.EventRating;
import ru.practicum.service.user.event.UserEvent;
import ru.practicum.service.user.event.UserEventRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final UserEventRepository userEventRepository;
    private final SimilarityRepository similarityRepository;

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(
            UserPredictionsRequestProto userPredictionsRequest
    ) {
        Sort sort = Sort.by(Sort.Order.desc("timestamp"));
        Pageable pageable = PageRequest.of(0, userPredictionsRequest.getMaxResult(), sort);
        Collection<Long> userActionIds = getUserActionIds(userPredictionsRequest, pageable);
        if (userActionIds.isEmpty()) {
            return Stream.empty();
        }
        Collection<Long> similarEvents = getSimilarEvents(userPredictionsRequest, userActionIds);

        Map<Long, List<NearestNeighborsProjection>> nearestNeighbors = 
                getNearestNeighbors(userPredictionsRequest, similarEvents);

        Map<Long, Double> nearestNeighborActions = 
                getNearestNeighborActions(userPredictionsRequest, nearestNeighbors);

        Collection<RecommendedEventProto> result = new ArrayList<>();
        return getCalculatedRecommendations(nearestNeighbors, nearestNeighborActions, result);
    }

    private static Stream<RecommendedEventProto> getCalculatedRecommendations(
            Map<Long, List<NearestNeighborsProjection>> nearestNeighbors,
            Map<Long, Double> nearestNeighborActions,
            Collection<RecommendedEventProto> result
    ) {
        for (Map.Entry<Long, List<NearestNeighborsProjection>> entry : nearestNeighbors.entrySet()) {
            double sumRating = 0.0;
            double sumSimilar = 0.0;
            for (NearestNeighborsProjection item : entry.getValue()) {
                sumRating += (nearestNeighborActions.getOrDefault(item.getNeighborEventId(), 0.0) * item.getScore());
                sumSimilar += item.getScore();
            }
            double score = sumRating / sumSimilar;
            result.add(RecommendedEventProto.newBuilder()
                    .setEventId(entry.getKey())
                    .setScore(score)
                    .build());
        }

        return result.stream()
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed());
    }

    private Map<Long, List<NearestNeighborsProjection>> getNearestNeighbors(
            UserPredictionsRequestProto userPredictionsRequest, 
            Collection<Long> similarEvents
    ) {
        return similarityRepository.getNearestNeighbors(similarEvents, userPredictionsRequest.getUserId()).stream()
                .collect(Collectors.groupingBy(NearestNeighborsProjection::getEventId));
    }

    private List<Long> getSimilarEvents(
            UserPredictionsRequestProto userPredictionsRequest, 
            Collection<Long> userActionIds
    ) {
        return similarityRepository.getSimilarEventsForRecommended(
                userActionIds,
                userPredictionsRequest.getUserId(),
                userPredictionsRequest.getMaxResult()
        );
    }

    private List<Long> getUserActionIds(UserPredictionsRequestProto userPredictionsRequest, Pageable pageable) {
        return userEventRepository.findByUserId(userPredictionsRequest.getUserId(), pageable).stream()
                .map(UserEvent::getEventId)
                .toList();
    }

    private Map<Long, Double> getNearestNeighborActions(
            UserPredictionsRequestProto userPredictionsRequest, Map<Long, 
            List<NearestNeighborsProjection>> nearestNeighbors
    ) {
        return userEventRepository.findByEventIdInAndUserId(
                        nearestNeighbors.values().stream()
                                .flatMap(l -> l.stream()
                                        .map(NearestNeighborsProjection::getNeighborEventId))
                                .distinct()
                                .toList(),
                        userPredictionsRequest.getUserId()).stream()
                .collect(Collectors.toMap(
                        UserEvent::getEventId,
                        UserEvent::getRating));
    }

    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        return similarityRepository.getSimilarEvents(
                        request.getEventId(),
                        request.getUserId(),
                PageRequest.of(0, request.getMaxResult())).stream()
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getEventId())
                        .setScore(entry.getRating())
                        .build()
                );
    }

    @Override
    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> eventIds = request.getEventIdList();

        if (eventIds.isEmpty()) {
            return Stream.empty();
        }

        List<EventRating> results = userEventRepository.getTotalWeightsForEvents(eventIds);

        results.forEach(result -> System.out.println(result.getRating()));
        return results.stream()
                .map(result -> RecommendedEventProto.newBuilder()
                        .setEventId(result.getEventId())
                        .setScore(result.getRating())
                        .build())
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed());
    }
}
