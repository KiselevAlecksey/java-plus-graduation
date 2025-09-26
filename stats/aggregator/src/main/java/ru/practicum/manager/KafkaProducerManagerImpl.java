package ru.practicum.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import ru.practicum.config.KafkaConfig;
import ru.practicum.service.util.KafkaProducerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@ManagedResource(objectName = "ru.yandex.practicum.telemetry.aggregator:type=Kafka,name=KafkaProducerManager",
        description = "Kafka Producers Management")
public class KafkaProducerManagerImpl implements KafkaProducerManager {
    private final Map<String, Producer<String, SpecificRecordBase>> producers;
    private final KafkaConfig kafkaConfig;
    private final KafkaProducerFactory producerFactory;
    @Value("${aggregator.kafka.producer.properties.close-time}")
    private int closeProducerTime;
    private volatile String producerStatus = "RUNNING";

    public KafkaProducerManagerImpl(KafkaConfig kafkaConfig, KafkaProducerFactory producerFactory) {
        this.producers = new ConcurrentHashMap<>();
        this.kafkaConfig = kafkaConfig;
        this.producerFactory = producerFactory;

        kafkaConfig.getProducers().forEach((name, config) ->
                producers.put(name, producerFactory.createProducer(name, config.getProperties()))
        );
    }

    @Override
    @ManagedOperation(description = "Gracefully shutdown all Kafka producers")
    public void closeAllProducers() {
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger totalCount = new AtomicInteger();

        producers.forEach((name, producer) -> {
            totalCount.incrementAndGet();
            if (closeProducer(name, producer)) {
                successCount.incrementAndGet();
            }
        });

        if (successCount.get() == totalCount.get()) {
            producerStatus = "SHUTDOWN_COMPLETE";
        } else if (successCount.get() > 0) {
            producerStatus = "SHUTDOWN_PARTIAL";
        } else {
            producerStatus = "SHUTDOWN_FAILED";
        }
    }

    @Override
    @ManagedOperation(description = "Close specific producer by name")
    public boolean closeProducer(String producerName) {
        Producer<String, SpecificRecordBase> producer = producers.get(producerName);
        if (producer == null) {
            throw new IllegalArgumentException("Producer not found: " + producerName);
        }
        return closeProducer(producerName, producer);
    }

    private boolean closeProducer(String name, Producer<String, SpecificRecordBase> producer) {
        try {
            log.info("Closing {} producer", name);
            producer.flush();
            producer.close(Duration.ofSeconds(closeProducerTime));
            producers.remove(name);
            return true;
        } catch (Exception e) {
            log.error("Error closing {} producer", name, e);
            return false;
        }
    }

    @Override
    @ManagedOperation(description = "Flush all producers")
    public void flushAllProducers() {
        producers.forEach((name, producer) -> {
            try {
                producer.flush();
            } catch (Exception e) {
                log.error("Error flushing {} producer", name, e);
            }
        });
    }

    @Override
    @ManagedAttribute(description = "Current producers status")
    public String getProducerStatus() {
        return producerStatus;
    }

    @Override
    @ManagedAttribute(description = "List of active producers")
    public Set<String> getActiveProducers() {
        return Collections.unmodifiableSet(producers.keySet());
    }

    @Override
    @ManagedAttribute(description = "Get or create producer by name")
    public Producer<String, SpecificRecordBase> getActiveProducer(String name) {
        return producers.computeIfAbsent(name, k -> {
            KafkaConfig.ProducerConfig config = kafkaConfig.getProducers().get(name);
            if (config == null) {
                throw new IllegalArgumentException("No configuration found for producer: " + name);
            }
            return producerFactory.createProducer(name, config.getProperties());
        });
    }

    @Override
    @ManagedAttribute(description = "Close timeout in seconds")
    public int getCloseTimeout() {
        return closeProducerTime;
    }

    @Override
    @ManagedAttribute
    public void setCloseTimeout(int seconds) {
        this.closeProducerTime = seconds;
        log.info("Updated producer close timeout to {} seconds", seconds);
    }
}
