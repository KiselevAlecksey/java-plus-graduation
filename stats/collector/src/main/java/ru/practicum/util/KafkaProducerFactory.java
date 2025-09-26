package ru.practicum.util;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;
import ru.practicum.configuration.KafkaConfig;
import ru.practicum.kafka.serializer.GeneralAvroSerializer;

import java.util.Map;
import java.util.Properties;

@Component
public class KafkaProducerFactory {
    private final KafkaConfig config;

    public KafkaProducerFactory(KafkaConfig config) {
        this.config = config;
    }

    public Producer<String, SpecificRecordBase> createProducer(String producerName, Map<String, String> properties) {
        KafkaConfig.ProducerConfig producerConfig = config.getProducers().get(producerName);
        if (producerConfig == null) {
            throw new IllegalArgumentException("Unknown producer configuration: " + producerName);
        }

        Properties props = new Properties();
        props.putAll(producerConfig.getProperties());
        props.putAll(properties);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());

        return new KafkaProducer<>(props);
    }
}
