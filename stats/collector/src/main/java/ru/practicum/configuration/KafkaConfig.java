package ru.practicum.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties("collector.kafka")
public class KafkaConfig {
    private Map<String, ProducerConfig> producers;

    @Getter
    @Setter
    public static class ProducerConfig {
        private Map<String, String> properties;
        private List<TopicConfig> topics;
    }
}
