package ru.practicum.service.client;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;

import java.util.List;
import java.util.Map;

public interface Client {
    Consumer<String, SpecificRecordBase> getConsumer();

    Map<String, String> getTopics();

    void stop();

    List<String> getAllTopics();
}
