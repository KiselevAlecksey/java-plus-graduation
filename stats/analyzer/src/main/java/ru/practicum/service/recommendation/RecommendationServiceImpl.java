package ru.practicum.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.service.similarity.event.SimilarCandidate;
import ru.practicum.service.similarity.event.Similarity;
import ru.practicum.service.similarity.event.SimilarityRepository;
import ru.practicum.service.user.event.UserEventRepository;
import ru.practicum.service.user.event.EventRating;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final UserEventRepository userEventRepository;
    private final SimilarityRepository similarityRepository;

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {

        // Список всех мероприятий с которыми взаимодействовал пользователь
        List<Long> eventInteractionList = userEventRepository.findEventIdByUserId(request.getUserId());

        if (eventInteractionList.isEmpty()) return Stream.empty();

        // 1. Получить недавно просмотренные. Отсортировать по дате взаимодействия от новых
        // к старым и ограничить количество N
        List<Long> recentlyViewedEventList = userEventRepository.findRecentInteractions(
                request.getUserId(),
                PageRequest.of(0, request.getMaxResult())
        );
        // 2. Найти похожие новые, с которыми не взаимодействовал.
        // Отсортировать по коэффициенту подобия. Выбрать N самых похожих.
        Map<Long, Double> candidateEventsWithSimilarity = similarityRepository.findSimilarCandidates(
                recentlyViewedEventList,
                eventInteractionList,
                PageRequest.of(0, request.getMaxResult())
        ).stream()
                .collect(Collectors.toMap(
                        SimilarCandidate::getEventId,
                        SimilarCandidate::getSimilarity)
                );

        // Вычисление оценки для каждого нового мероприятия.
        // Найти K ближайших соседей для каждого нового похожего.
        Map<Long, List<Long>> similarCandidateWithNeighborsMap = getGroupedNeighbors(
                candidateEventsWithSimilarity.keySet().stream().toList(),
                eventInteractionList,
                request.getMaxResult()
        );

        List<Long> allNeighboursList = new ArrayList<>();

        similarCandidateWithNeighborsMap.forEach(
                (key, value) -> allNeighboursList.addAll(value.stream().toList())
        );

        // Таблица со всеми соседями в Key и их оценкой пользователя в Value
        Map<Long, Double> eventWithRatingMap = userEventRepository.findByEventIdIn(allNeighboursList).stream()
                .collect(Collectors.toMap(EventRating::getEventId, EventRating::getRating));
        // Таблица со всеми соседями в Key и их коэффициентом сходства в Value
        Map<Long, Double> similarityScoreMap = similarityRepository.findByEventAOrEventBIn(allNeighboursList).stream()
                .collect(Collectors.toMap(SimilarCandidate::getEventId, SimilarCandidate::getSimilarity));

        // Таблица со всеми НОВЫМИ мероприятиями в Key
        // и соседями в Key и их оценкой пользователя в Value в Value
        Map<Long, Map<Long, Double>> similarCandidateWithNeighborsWithUserRating = similarCandidateWithNeighborsMap
                .entrySet()
                .parallelStream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(neighborId -> {
                                    Double rating = eventWithRatingMap.get(neighborId);
                                    return rating != null ? Map.entry(neighborId, rating) : null;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue
                                ))
                ));

        // Таблица со всеми НОВЫМИ мероприятиями в Key
        // и соседями в Key и их коэффициентом сходства в Value в Value
        Map<Long, Map<Long, Double>> similarCandidateWithNeighborsWithSimilarity = similarCandidateWithNeighborsMap
                .entrySet()
                .parallelStream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(neighborId -> {
                                    Double similarity = similarityScoreMap.get(neighborId);
                                    return similarity != null ? Map.entry(neighborId, similarity) : null;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue
                                ))
                ));

        // Таблица со всеми НОВЫМИ мероприятиями в Key
        // и соседями в Key и их оценку пользователя * коэффициентом сходства = вес мероприятия в Value в Value
        Map<Long, Map<Long, Double>> weightEventScore = similarCandidateWithNeighborsWithUserRating
                .entrySet()
                .parallelStream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().parallelStream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        neighborEntry -> neighborEntry.getValue()
                                                * similarityScoreMap.get(entry.getKey())
                                        )
                                ))
                        );

        // Сложить сумму взвешенных оценок
        Map<Long, Double> sumWeightScore = weightEventScore.entrySet().parallelStream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        neighbour -> neighbour.getValue().values().parallelStream()
                                    .mapToDouble(Double::doubleValue)
                                    .sum()
                ));

        //и разделить на сумму коэффициентов сходства
        Map<Long, Double> sumCoefficientSimilarity = similarCandidateWithNeighborsWithSimilarity
                .entrySet()
                .parallelStream()
                .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                neighbour -> neighbour.getValue().values().parallelStream()
                                        .mapToDouble(Double::doubleValue)
                                        .sum()
                ));

        Map<Long, Double> predictionScore = sumWeightScore.entrySet().parallelStream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / sumCoefficientSimilarity.get(entry.getKey())
                ));

        // сложить для каждого candidateEventId в мапе sumWeightScore сумму оценок value из sumWeightScore - числитель
        // то есть должен получиться набор сумм для каждого id
        // сейчас не хватает мапы как similarCandidateWithNeighborsWithUserRating,
        // ---Далее знаменатель из candidateEventsWithSimilarity сложить сумму из values кэф сходства
        // числитель/знаменатель =

        return predictionScore.entrySet().parallelStream()
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build());
    }

    private Map<Long, List<Long>> getGroupedNeighbors(
            List<Long> eventIds,
            List<Long> interactionEvents,
            int k) {

        List<Object[]> rawData = similarityRepository.findAllNeighbors(eventIds, interactionEvents, k);

        return rawData.stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row[1], // baseEvent
                        Collectors.mapping(
                                row -> (Long) row[0], // neighbor
                                Collectors.toList()
                        )
                ));
    }

    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        List<Long> eventInteractionList = userEventRepository.findEventIdByUserId(request.getUserId());

        if (eventInteractionList.isEmpty()) {
            return Stream.empty();
        }

        List<Similarity> similarEventList = similarityRepository.findSimilarEventsForMultipleIds(
                List.of(request.getEventId()),
                eventInteractionList,
                PageRequest.of(0, request.getMaxResult())
        );

        return similarEventList.stream()
                .map(similarity -> RecommendedEventProto.newBuilder()
                        .setEventId(
                                similarity.getEventA().equals(request.getEventId())
                                ? similarity.getEventB()
                                : similarity.getEventA()
                        )
                        .setScore(similarity.getSimilarity())
                        .build()
                );
    }

    @Override
    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> eventIds = request.getEventIdList();

        if (eventIds.isEmpty()) {
            return Stream.empty();
        }

        List<Object[]> totalWeightsForEvents = userEventRepository.getTotalWeightsForEvents(eventIds);

        Map<Long, Double> weightMap = totalWeightsForEvents.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Double) result[1]
                ));

        return eventIds.stream()
                .map(eventId -> RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(weightMap.getOrDefault(eventId, 0.0))
                        .build());
    }
}
