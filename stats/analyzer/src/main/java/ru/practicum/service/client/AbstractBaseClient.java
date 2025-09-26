package ru.practicum.service.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import ru.practicum.config.KafkaConfig;
import ru.practicum.config.TopicConfig;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractBaseClient implements Client {

    protected Consumer<String, SpecificRecordBase> consumer;
    protected final KafkaConfig config;
    private final String consumerConfigName;

    public AbstractBaseClient(KafkaConfig config, String consumerConfigName) {
        this.config = config;
        this.consumerConfigName = consumerConfigName;
    }

    @Override
    public Consumer<String, SpecificRecordBase> getConsumer() {
        if (consumer == null) {
            init();
        }
        return consumer;
    }

    @Override
    public Map<String, String> getTopics() {
        return getTopicConfigs().stream()
                .collect(Collectors.toMap(
                        TopicConfig::getName,
                        TopicConfig::getValue
                ));
    }

    @Override
    public List<String> getAllTopics() {
        return getTopicConfigs().stream()
                .map(TopicConfig::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public void stop() {
        if (consumer != null) {
            consumer.close();
            consumer = null;
        }
    }

    protected void init() {
        Map<String, String> configMap = getConsumerConfig().getProperties();
        Properties props = new Properties();
        props.putAll(configMap);

        consumer = new KafkaConsumer<>(props);
    }

    protected KafkaConfig.ConsumerConfig getConsumerConfig() {
        KafkaConfig.ConsumerConfig consumerConfig = config.getConsumers().get(consumerConfigName);
        if (consumerConfig == null) {
            throw new IllegalStateException("Конфигурация для '" + consumerConfigName + "' не найдена");
        }
        return consumerConfig;
    }

    protected List<TopicConfig> getTopicConfigs() {
        return getConsumerConfig().getTopics();
    }
}
