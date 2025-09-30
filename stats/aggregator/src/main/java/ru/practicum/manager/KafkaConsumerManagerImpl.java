package ru.practicum.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import ru.practicum.config.KafkaConfig;
import ru.practicum.service.util.KafkaConsumerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.practicum.manager.KafkaStatus.*;

@Slf4j
@ManagedResource(objectName = "ru.yandex.practicum.telemetry.aggregator:type=Kafka,name=KafkaEventConsumerManager",
        description = "Kafka Event Consumers Management")
public class KafkaConsumerManagerImpl implements KafkaConsumerManager {
    private final Map<String, Consumer<String, SpecificRecordBase>> consumers;
    private final KafkaConfig kafkaConfig;
    private final KafkaConsumerFactory consumerFactory;
    private volatile String status = RUNNING.name();
    @Value("${aggregator.kafka.consumer.properties.close-time}")
    private int consumerCloseTimeout;

    public KafkaConsumerManagerImpl(KafkaConfig kafkaConfig, KafkaConsumerFactory consumerFactory) {
        this.consumers = new ConcurrentHashMap<>();
        this.kafkaConfig = kafkaConfig;
        this.consumerFactory = consumerFactory;

        kafkaConfig.getConsumers().forEach((name, config) ->
                consumers.put(name, consumerFactory.createConsumer(name, config.getProperties()))
        );
    }

    @Override
    @ManagedAttribute(description = "Get or create producer by name")
    public Consumer<String, SpecificRecordBase> getActiveConsumer(String name) {
        return consumers.computeIfAbsent(name, k -> {
            KafkaConfig.ConsumerConfig config = kafkaConfig.getConsumers().get(name);
            if (config == null) {
                throw new IllegalArgumentException("No configuration found for producer: " + name);
            }
            return consumerFactory.createConsumer(name, config.getProperties());
        });
    }

    @Override
    @ManagedOperation(description = "Gracefully shutdown all Kafka consumers")
    public void closeAllConsumers() {
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger totalCount = new AtomicInteger();

        consumers.forEach((name, consumer) -> {
            totalCount.incrementAndGet();
            if (closeConsumer(name, consumer)) {
                successCount.incrementAndGet();
            }
        });

        if (successCount.get() == totalCount.get()) {
            status = SHUTDOWN_COMPLETE.name();
        } else if (successCount.get() > 0) {
            status = SHUTDOWN_PARTIAL.name();
        } else {
            status = SHUTDOWN_FAILED.name();
        }
    }

    @Override
    @ManagedOperation(description = "Close specific consumer by name")
    public boolean closeConsumer(String consumerName) {
        Consumer<String, ?> consumer = consumers.get(consumerName);
        if (consumer == null) {
            throw new IllegalArgumentException("Consumer not found: " + consumerName);
        }
        return closeConsumer(consumerName, consumer);
    }

    private boolean closeConsumer(String name, Consumer<String, ?> consumer) {
        try {
            log.info("Closing {} consumer", name);
            consumer.unsubscribe();
            consumer.close(Duration.ofSeconds(consumerCloseTimeout));
            consumers.remove(name);
            return true;
        } catch (Exception e) {
            log.error("Error closing {} consumer", name, e);
            return false;
        }
    }

    @Override
    @ManagedOperation(description = "Pause all consumers")
    public void pauseAllConsumers() {
        consumers.forEach((name, consumer) -> {
            Set<TopicPartition> assignments = consumer.assignment();
            if (assignments != null && !assignments.isEmpty()) {
                consumer.pause(assignments);
            }
        });
        status = PAUSED.name();
    }

    @Override
    @ManagedOperation(description = "Resume all consumers")
    public void resumeAllConsumers() {
        consumers.forEach((name, consumer) -> {
            Set<TopicPartition> assignments = consumer.assignment();
            if (assignments != null && !assignments.isEmpty()) {
                consumer.resume(assignments);
            }
        });
        status = RUNNING.name();
    }

    @Override
    @ManagedAttribute(description = "Current consumers status")
    public String getStatus() {
        return status;
    }

    @Override
    @ManagedAttribute(description = "List of active consumers")
    public Set<String> getActiveConsumers() {
        return Collections.unmodifiableSet(consumers.keySet());
    }

    @Override
    @ManagedAttribute(description = "Close timeout in seconds")
    public int getCloseTimeout() {
        return consumerCloseTimeout;
    }
}
