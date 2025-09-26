package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.client.AggregatorClient;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorStarter {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(100);
    private static final int AMOUNT_PART_COMMIT = 10;
    private static final String CONSUMER_NAME = "users-events";
    private static final String PRODUCER_NAME = "similarity-events";
    public static final String USERS_EVENTS = "users-events";
    public static final String SIMILARITY_EVENTS = "similarity-events";

    @Value("${ratings.view:0.4}")
    private double viewRating;

    @Value("${ratings.register:0.8}")
    private double registerRating;

    @Value("${ratings.like:1.0}")
    private double likeRating;

    private final AggregatorClient client;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();
    // ключ — это мероприятие, а значение — ещё одно отображение,
    // где ключ — пользователь, а значение — максимальный вес из всех его действий с этим мероприятием:
    // Map<Event, Map<User, Weight>>
    private final Map<Long, Map<Long, Double>> userMaxWeightEventMap = new ConcurrentHashMap<>();
    // общие суммы весов каждого из мероприятий, где ключ — мероприятие,
    // а значение — сумма весов действий пользователей с ним
    private final Map<Long, Double> sumUserWeightMap = new ConcurrentHashMap<>();
    // Сумма минимальных весов для каждой пары мероприятий.
    // Тогда ключом будет одно из мероприятий, а значением — ещё одно отображение,
    // где ключ — второе мероприятие, а значение — сумма их минимальных весов
    private final Map<Long, Map<Long, Double>> userMinWeightSumEventMap = new HashMap<>();

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(client.getConsumer(CONSUMER_NAME)::wakeup));
        try {
            consumerSubscribe();
            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = client.getConsumer(CONSUMER_NAME).poll(CONSUME_ATTEMPT_TIMEOUT);
                int count = 0;
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    log.info("{}", record);
                    UserActionAvro userAction = (UserActionAvro) record.value();

                    Optional<EventSimilarityAvro> similarityAvro = calculateSimilarity(userAction);

                    similarityAvro.ifPresent(s -> {
                        sendProducerEvent(s);
                        log.info("{}", s);
                    });
                    manageOffsets(record, count, client.getConsumer(CONSUMER_NAME));
                }
                client.getConsumer(CONSUMER_NAME).commitAsync();
            }
        } catch (WakeupException ignored) {
        } finally {
            try {
                client.getConsumer(CONSUMER_NAME).commitSync(currentOffsets);
            } finally {
                log.info("Закрываем продюсер и консьюмер");
                client.closeConsumerAndProducer(CONSUMER_NAME, PRODUCER_NAME);
            }
        }
    }

    private Optional<EventSimilarityAvro> calculateSimilarity(UserActionAvro event) {
        Map<Long, Double> mapUserRating =
                new HashMap<>(userMaxWeightEventMap.getOrDefault(event.getEventId(), Map.of()));

        // Обработка ситуации если рейтинг в сообщении не нужно обновлять, возврат существующего сходства
        // исправить на числитель = сумма минимальных весов для двух мероприятий
        // Знаменатель = произведение квадратных корней от общих сумм весов для каждого мероприятия
        // То есть userMinWeightSumEventMap.getValue(A).getValue(B) / sqrt.sumUserWeightMap.getValue(A) * sqrt.sumUserWeightMap.getValue(B)
        if (!mapUserRating.isEmpty()
                && mapUserRating.getOrDefault(event.getUserId(), 0.0)
                > getRating(event.getActionType())
                && canCompareEvents()
        ) {
            return userMinWeightSumEventMap.entrySet().stream()
                    .map(entry -> EventSimilarityAvro.newBuilder()
                            .setEventA(entry.getKey())
                            .setEventB(entry.getValue().keySet().stream().findFirst().orElse(0L))
                            .setScore(
                                    entry.getValue().values().stream().findFirst().orElse(0.0)
                                    / Math.sqrt(sumUserWeightMap.get(entry.getKey())
                                            * sumUserWeightMap.get(entry.getValue().keySet().stream().findFirst().orElse(0L)))
                            )
                            .setTimestamp(Instant.now())
                            .build())
                    .findFirst();
        }
        // Добавляем оценку, т.к. проверили что это либо новая запись, либо оценка выше
        userMaxWeightEventMap.put(
                event.getEventId(),
                Map.of(
                        event.getUserId(),
                        getRating(event.getActionType())
                )
        );
        // Пересчитать сумму весов если новое действие или если изменилось
        sumUserWeightMap.put(
                event.getEventId(),
                userMaxWeightEventMap.get(event.getEventId()).values().parallelStream()
                        .mapToDouble(Double::doubleValue)
                        .sum()
        );

        if (canCompareEvents()) {

            userMinWeightSumEventMap.put(event.getEventId() , userMaxWeightEventMap.keySet().stream()
                    .filter(otherEventId -> !otherEventId.equals(event.getEventId()))
                    .filter(otherEventId -> event.getEventId() < otherEventId)
                    .collect(Collectors.toMap(
                            otherEventId -> otherEventId,
                            otherEventId -> compareEvents1(
                                    userMaxWeightEventMap.get(event.getEventId()),
                                    userMaxWeightEventMap.get(otherEventId)
                            )
                    )));
        }
        /*
        // Обработка ситуации, если это первое действие с новым мероприятием
        if (mapUserRating.isEmpty()) {


        }
        // Обработка ситуации если рейтинг оценки пользователя повысился
        boolean isRatingChange = userMaxWeightEventMap.get(event.getEventId()).get(event.getUserId())
                > getRating(event.getActionType());

        if (isRatingChange) {


        }

        userMinWeightSumEventMap.putAll(userMaxWeightEventMap.keySet().stream()
                .collect(Collectors.toMap(
                        event1 -> event1,
                        event1 -> userMaxWeightEventMap.keySet().stream()
                                .filter(event2 -> !event2.equals(event1))
                                .collect(Collectors.toMap(
                                        event2 -> event2,
                                        event2 -> compareEvents1(userMaxWeightEventMap.get(event1),
                                                userMaxWeightEventMap.get(event2))
                                ))
                )));*/

        return canCompareEvents() ? userMinWeightSumEventMap.entrySet().stream()
                .map(entry -> {
                    long first  = Math.min(entry.getKey(), entry.getValue().keySet().stream().findFirst().orElse(0L));
                    long second = Math.max(entry.getKey(), entry.getValue().keySet().stream().findFirst().orElse(0L));
                    return EventSimilarityAvro.newBuilder()
                            .setEventA(first)
                            .setEventB(second)
                            .setScore(
                                    entry.getValue().values().stream().findFirst().orElse(0.0)
                                            / Math.sqrt(sumUserWeightMap.getOrDefault(entry.getKey(), 0.0)
                                            * sumUserWeightMap.getOrDefault(
                                                    entry.getValue().keySet().stream().findFirst().orElse(0L), 1.0))
                            )
                            .setTimestamp(Instant.now())
                            .build();
                })
                .findFirst() : Optional.empty();
    }

    private double compareEvents1(Map<Long, Double> userWeightMap1, Map<Long, Double> userWeightMap2) {
        // Находим общих пользователей
        Set<Long> commonUsers = new HashSet<>(userWeightMap1.keySet());
        commonUsers.retainAll(userWeightMap2.keySet());

        if (commonUsers.isEmpty()) {
            return 0.0; // Нет общих пользователей - схожесть нулевая
        }

        // Ваш подход с минимумами (исправленный)
        double sumOfMin = commonUsers.stream()
                .mapToDouble(user -> Math.min(userWeightMap1.get(user), userWeightMap2.get(user)))
                .sum();

        return sumOfMin;
    }

    public boolean canCompareEvents() {
        Map<Long, Long> userEventCount = userMaxWeightEventMap.values().stream()
                .flatMap(userMap -> userMap.keySet().stream())
                .collect(Collectors.groupingBy(
                        userId -> userId,
                        Collectors.counting()
                ));

        return userEventCount.values().stream()
                .anyMatch(count -> count >= 2);
    }

    private double compareEvents(Map<Long, Double> userWeightMap1, Map<Long, Double> userWeightMap2) {
        return userWeightMap1.entrySet().parallelStream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Math.min(entry.getValue(), userWeightMap2.get(entry.getKey()))
                ))
                .values()
                .parallelStream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }



    private void sendProducerEvent(EventSimilarityAvro event) {

        String topic = client.getProducerTopics(PRODUCER_NAME).get(SIMILARITY_EVENTS);
        client.getProducer(PRODUCER_NAME)
                .send(new ProducerRecord<>(topic, event));
    }

    private void consumerSubscribe() {
        client.getConsumer(CONSUMER_NAME)
                .subscribe(List.of(client.getConsumerTopics(CONSUMER_NAME).get(USERS_EVENTS)));
    }

    private void manageOffsets(
            ConsumerRecord<String, SpecificRecordBase> record,
            int count,
            Consumer<String, SpecificRecordBase> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % AMOUNT_PART_COMMIT == 0) {
            consumer.commitAsync(currentOffsets, (offsets, e) -> {
                if (e != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, e);
                }
            });
        }
    }

    private double getRating(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> viewRating;
            case REGISTER -> registerRating;
            case LIKE -> likeRating;
            default -> throw new IllegalArgumentException("Unavailable action type: " + type);
        };
    }
}
