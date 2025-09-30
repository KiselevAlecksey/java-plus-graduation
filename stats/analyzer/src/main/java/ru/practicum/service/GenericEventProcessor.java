package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import ru.practicum.service.client.Client;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static ru.practicum.service.OffsetManager.manageOffsets;

@Slf4j
public class GenericEventProcessor implements EventProcessor, Runnable  {
    protected static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(100);
    protected static final int AMOUNT_PART_COMMIT = 10;

    protected final Client client;
    protected final EventHandler handler;
    protected final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    protected final String processorType;

    public GenericEventProcessor(
            Client eventClient,
            EventHandler eventHandler,
            String processorType
    ) {
        this.client = eventClient;
        this.handler = eventHandler;
        this.processorType = processorType;
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(client.getConsumer()::wakeup));
        try {
            consumerSubscribe();
            while (true) {
                try {
                    ConsumerRecords<String, SpecificRecordBase> records =
                            client.getConsumer().poll(CONSUME_ATTEMPT_TIMEOUT);
                    int count = 0;
                    for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                        SpecificRecord event = record.value();
                        log.info("{}", event);
                        handler.handle(event);
                        manageOffsets(
                                currentOffsets,
                                AMOUNT_PART_COMMIT,
                                record,
                                count,
                                client.getConsumer()
                        );
                    }
                    client.getConsumer().commitAsync();
                } catch (WakeupException e) {
                    throw new WakeupException();
                } catch (Exception e) {
                    log.error("Ошибка ", e);
                }
            }
        } catch (WakeupException ignored) {
        } finally {
            try {
                client.getConsumer().commitSync(currentOffsets);
            } finally {
                log.info("Закрываем продюсер и HubConsumer");
                client.stop();
            }
        }
    }

    private void consumerSubscribe() {
        client.getConsumer().subscribe(client.getAllTopics());
    }

    @Override
    public String getProcessorType() {
        return processorType;
    }
}

