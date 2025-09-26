package ru.practicum.manager;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

import java.util.Set;

public interface KafkaConsumerManager {
    @ManagedAttribute(description = "Get or create producer by name")
    Consumer<String, SpecificRecordBase> getActiveConsumer(String name);

    @ManagedOperation(description = "Gracefully shutdown all Kafka consumers")
    void closeAllConsumers();

    @ManagedOperation(description = "Close specific consumer by name")
    boolean closeConsumer(String consumerName);

    @ManagedOperation(description = "Pause all consumers")
    void pauseAllConsumers();

    @ManagedOperation(description = "Resume all consumers")
    void resumeAllConsumers();

    @ManagedAttribute(description = "Current consumers status")
    String getStatus();

    @ManagedAttribute(description = "List of active consumers")
    Set<String> getActiveConsumers();

    @ManagedAttribute(description = "Close timeout in seconds")
    int getCloseTimeout();
}
