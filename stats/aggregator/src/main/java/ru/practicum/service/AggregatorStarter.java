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
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;
import ru.practicum.config.RatingsConfig;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.client.Client;

import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.config.KafkaConfig.CONSUMER_NAME;
import static ru.practicum.config.KafkaConfig.PRODUCER_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorStarter {
    private final KafkaConfig kafkaConfig;
    private final RatingsConfig ratingsConfig;
    private final Client client;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final Map<Long, Map<Long, Double>> weights = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();
    private final Map<Long, Double> eventWeightSums = new HashMap<>();

    public void start() {
        KafkaConfig.ConsumerConfig consumerConfig = kafkaConfig.getConsumers().get(CONSUMER_NAME);
        Runtime.getRuntime().addShutdownHook(new Thread(client.getConsumer(CONSUMER_NAME)::wakeup));
        try {
            client.getConsumer(CONSUMER_NAME)
                    .subscribe(List.of(client.getConsumerTopics(CONSUMER_NAME).get(CONSUMER_NAME)));
            while (true) {
                try {
                    ConsumerRecords<String, SpecificRecordBase> records = client.getConsumer(CONSUMER_NAME).poll(consumerConfig.getAttemptTimeout());
                    int count = 0;
                    for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                        log.info("{}", record);
                        UserActionAvro userAction = (UserActionAvro) record.value();
                        Collection<Optional<EventSimilarityAvro>> similarityAvro = calculateSimilarity(userAction);

                        similarityAvro.forEach(s -> s.ifPresent(sa -> {
                            sendProducerEvent(sa);
                            log.info("Отправка в продюсер {}", sa);
                        }));
                        manageOffsets(record, count, client.getConsumer(CONSUMER_NAME));
                    }
                    client.getConsumer(CONSUMER_NAME).commitAsync();
                } catch (WakeupException e) {
                    throw new WakeupException();
                } catch (Exception e) {
                    log.error("Ошибка ", e);
                }
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

    private void manageOffsets(
            ConsumerRecord<String, SpecificRecordBase> record,
            int count,
            Consumer<String, SpecificRecordBase> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % kafkaConfig.getConsumers().get(CONSUMER_NAME).getAmountPartCommit() == 0) {
            consumer.commitAsync(currentOffsets, (offsets, e) -> {
                if (e != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, e);
                }
            });
        }
    }

    private List<Optional<EventSimilarityAvro>> calculateSimilarity(UserActionAvro userAction) {
        Double oldWeightEventA = weights
                .computeIfAbsent(userAction.getEventId(), e -> new HashMap<>())
                .getOrDefault(userAction.getUserId(), 0.0);
        double newWeightEventA = Math.max(
                getRating(userAction.getActionType()),
                oldWeightEventA);

        if (oldWeightEventA.equals(newWeightEventA)) {
            return List.of();
        }

        Double oldWeightSumEventA = eventWeightSums.getOrDefault(userAction.getEventId(), 0.0);
        double newWeightSumEventA = oldWeightSumEventA + (newWeightEventA - oldWeightEventA);
        weights.get(userAction.getEventId()).put(userAction.getUserId(), newWeightEventA);
        eventWeightSums.put(userAction.getEventId(), newWeightSumEventA);

        return weights.keySet().stream()
                .filter(eventId -> !eventId.equals(userAction.getEventId()))
                .map(eventId -> {
                    Double oldWeightEventB = weights.get(eventId).getOrDefault(userAction.getUserId(), 0.0);
                    if (oldWeightEventB.equals(0.0)) {
                        return Optional.<EventSimilarityAvro>empty();
                    }

                    Double oldWeightSumEventB = eventWeightSums.getOrDefault(eventId, 0.0);
                    Double oldMin = Math.min(oldWeightEventA, oldWeightEventB);
                    Double newMin = Math.min(newWeightEventA, oldWeightEventB);
                    Double oldMinWeightsSums = getMinWeightsSums(userAction.getEventId(), eventId);
                    double newMinWeightsSums = oldMinWeightsSums + (newMin - oldMin);

                    if (!oldMin.equals(newMin)) {
                        putMinWeightsSums(userAction.getEventId(), eventId, newMinWeightsSums);
                    }

                    double score = newMinWeightsSums
                            / (Math.sqrt(newWeightSumEventA)
                            * Math.sqrt(oldWeightSumEventB));
                    return getEventSimilarityAvro(userAction, eventId, score);
                })
                .filter(Optional::isPresent)
                .collect(Collectors.toList());
    }

    private static Optional<EventSimilarityAvro> getEventSimilarityAvro(
            UserActionAvro userAction,
            Long eventId,
            double score
    ) {
        long eventA = Math.min(userAction.getEventId(), eventId);
        long eventB = Math.max(userAction.getEventId(), eventId);

        return Optional.of(EventSimilarityAvro.newBuilder()
                .setEventA(eventA)
                .setEventB(eventB)
                .setScore(score)
                .setTimestamp(userAction.getTimestamp())
                .build());
    }

    private void putMinWeightsSums(Long eventA, Long eventB, Double sum) {
        Long first = Math.min(eventA, eventB);
        Long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private Double getMinWeightsSums(Long eventA, Long eventB) {
        Long first = Math.min(eventA, eventB);
        Long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private void sendProducerEvent(EventSimilarityAvro event) {
        String topic = client.getProducerTopics(PRODUCER_NAME).get(PRODUCER_NAME);
        client.getProducer(PRODUCER_NAME)
                .send(new ProducerRecord<>(topic, event));
    }

    private double getRating(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> ratingsConfig.getView();
            case REGISTER -> ratingsConfig.getRegister();
            case LIKE -> ratingsConfig.getLike();
            default -> throw new IllegalArgumentException("Unavailable action type: " + type);
        };
    }
}
