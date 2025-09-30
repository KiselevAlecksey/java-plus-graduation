package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class OffsetManager {
    private static final int DEFAULT_AMOUNT_PART_COMMIT = 10;

    public static void manageOffsets(
            Map<TopicPartition, OffsetAndMetadata> currentOffsets,
            int amountPartCommit,
            ConsumerRecord<String, SpecificRecordBase> record,
            int count,
            Consumer<String, SpecificRecordBase> consumer) {
        if (currentOffsets == null) {
            currentOffsets = new HashMap<>();
        }
        amountPartCommit = amountPartCommit == 0 ? DEFAULT_AMOUNT_PART_COMMIT : amountPartCommit;
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % amountPartCommit == 0) {
            consumer.commitAsync(currentOffsets, (offsets, e) -> {
                if (e != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, e);
                }
            });
        }
    }
}
