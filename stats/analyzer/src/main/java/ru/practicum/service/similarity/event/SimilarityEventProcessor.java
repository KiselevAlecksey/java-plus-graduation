package ru.practicum.service.similarity.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.service.EventHandler;
import ru.practicum.service.GenericEventProcessor;
import ru.practicum.service.client.Client;
import ru.practicum.service.client.SimilarityEventClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static ru.practicum.service.OffsetManager.manageOffsets;

@Slf4j
@Component
public class SimilarityEventProcessor extends GenericEventProcessor {
    public SimilarityEventProcessor(
            @Qualifier("similarityClient") Client similarityEventClient,
            @Qualifier("similarityEventHandler") EventHandler similarityEventHandler
    ) {
        super(similarityEventClient, similarityEventHandler, "SIMILARITY_EVENT");
    }
}
