package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties("aggregator.kafka")
public class KafkaConfig {
    public static final String CONSUMER_NAME = "users-events";
    public static final String PRODUCER_NAME = "similarity-events";
    private Map<String, ProducerConfig> producers;
    private Map<String, ConsumerConfig> consumers;

    @Getter
    @Setter
    public static class ProducerConfig {
        private Map<String, String> properties;
        private List<TopicConfig> topics;
    }

    @Getter
    @Setter
    public static class ConsumerConfig {
        private Map<String, String> properties;
        private List<TopicConfig> topics;
        private Duration attemptTimeout = Duration.ofMillis(100);
        private int amountPartCommit = 10;
    }

    @Getter
    @Setter
    @ConfigurationProperties("aggregator")
    public static class Ratings {
        private double view = 0.4;
        private double register = 0.8;
        private double like = 1.0;
    }
}
