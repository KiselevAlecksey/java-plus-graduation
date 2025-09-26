package ru.practicum.service.util;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;
import ru.practicum.config.TopicConfig;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KafkaConsumerFactory {
    private final KafkaConfig config;

    /**
     * Создаёт Kafka Consumer с настройками по умолчанию из конфига
     * @param consumerName имя конфигурации потребителя
     */
    public Consumer<String, SpecificRecordBase> createConsumer(String consumerName) {
        return createConsumer(consumerName, Map.of());
    }

    /**
     * Создаёт Kafka Consumer с возможностью переопределения свойств
     * @param consumerName имя конфигурации потребителя
     * @param properties дополнительные/переопределяющие свойства
     */
    public Consumer<String, SpecificRecordBase> createConsumer(String consumerName, Map<String, String> properties) {
        KafkaConfig.ConsumerConfig consumerConfig = config.getConsumers().get(consumerName);
        if (consumerConfig == null) {
            throw new IllegalArgumentException("Unknown consumer configuration: " + consumerName);
        }

        Properties props = new Properties();
        props.putAll(consumerConfig.getProperties());
        props.putAll(properties);

        if (!props.containsKey(ConsumerConfig.CLIENT_ID_CONFIG)) {
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer-" + consumerName);
        } else {
            props.put(ConsumerConfig.CLIENT_ID_CONFIG,
                    props.getProperty(ConsumerConfig.CLIENT_ID_CONFIG) + "-" + consumerName);
        }
        if (!props.containsKey(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)) {
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        }

        if (!props.containsKey(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)) {
            throw new IllegalStateException("Value deserializer must be specified in config for consumer: " + consumerName);
        }

        return new KafkaConsumer<>(props);
    }

    /**
     * Создаёт Kafka Consumer для конкретного типа событий
     * @param consumerName имя конфигурации потребителя
     * @param eventType тип события (определяет десериализатор)
     */
    public <T extends SpecificRecordBase> Consumer<String, T> createTypedConsumer(
            String consumerName,
            Class<T> eventType,
            Class<? extends Deserializer<T>> deserializerClass) {

        Map<String, String> props = Map.of(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass.getName()
        );

        @SuppressWarnings("unchecked")
        Consumer<String, T> consumer = (Consumer<String, T>) createConsumer(consumerName, props);
        return consumer;
    }

    /**
     * Создаёт и подписывает Consumer на указанный топик
     * @param consumerName имя конфигурации
     * @param topicConfigName имя топика из конфига
     * @param eventType тип события
     * @param deserializerClass класс десериализатора
     */
    public <T extends SpecificRecordBase> Consumer<String, T> createAndSubscribeConsumer(
            String consumerName,
            String topicConfigName,
            Class<T> eventType,
            Class<? extends Deserializer<T>> deserializerClass) {

        KafkaConfig.ConsumerConfig consumerConfig = config.getConsumers().get(consumerName);
        if (consumerConfig == null) {
            throw new IllegalArgumentException("Unknown consumer configuration: " + consumerName);
        }

        String topicName = consumerConfig.getTopics().stream()
                .filter(t -> t.getName().equals(topicConfigName))
                .findFirst()
                .map(TopicConfig::getValue)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic '" + topicConfigName + "' not found in consumer '" + consumerName + "' config"));

        Consumer<String, T> consumer = createTypedConsumer(consumerName, eventType, deserializerClass);
        consumer.subscribe(Collections.singletonList(topicName));
        return consumer;
    }

    /**
     * Возвращает список топиков для указанного consumer'a
     */
    public List<String> getConsumerTopics(String consumerName) {
        KafkaConfig.ConsumerConfig consumerConfig = config.getConsumers().get(consumerName);
        if (consumerConfig == null) {
            throw new IllegalArgumentException("Unknown consumer configuration: " + consumerName);
        }
        return consumerConfig.getTopics().stream()
                .map(TopicConfig::getValue)
                .collect(Collectors.toList());
    }
}
