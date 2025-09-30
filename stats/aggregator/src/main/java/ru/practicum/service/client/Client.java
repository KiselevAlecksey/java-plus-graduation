package ru.practicum.service.client;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Map;

public interface Client {
    Producer<String, SpecificRecordBase> getProducer(String producerName);

    Map<String, String> getProducerTopics(String producerName);

    Consumer<String, SpecificRecordBase> getConsumer(String consumerName);

    Map<String, String> getConsumerTopics(String consumerName);

    public void closeConsumerAndProducer(String consumerName, String producerName);
}