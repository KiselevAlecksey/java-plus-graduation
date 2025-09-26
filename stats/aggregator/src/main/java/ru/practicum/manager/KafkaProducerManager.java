package ru.practicum.manager;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

import java.util.Set;

public interface KafkaProducerManager {
    @ManagedOperation(description = "Gracefully shutdown all Kafka producers")
    void closeAllProducers();

    @ManagedOperation(description = "Close specific producer by name")
    boolean closeProducer(String producerName);

    @ManagedOperation(description = "Flush all producers")
    void flushAllProducers();

    @ManagedAttribute(description = "Current producers status")
    String getProducerStatus();

    @ManagedAttribute(description = "List of active producers")
    Set<String> getActiveProducers();

    @ManagedAttribute(description = "Get or create producer by name")
    Producer<String, SpecificRecordBase> getActiveProducer(String name);

    @ManagedAttribute(description = "Close timeout in seconds")
    int getCloseTimeout();

    @ManagedAttribute
    void setCloseTimeout(int seconds);
}
