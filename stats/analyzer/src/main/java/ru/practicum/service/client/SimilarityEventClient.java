package ru.practicum.service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;

@Component("similarityClient")
@Qualifier("similarityClient")
public class SimilarityEventClient extends AbstractBaseClient {
    private static final String CONSUMER_NAME = "similarity";

    public SimilarityEventClient(KafkaConfig config) {
        super(config, CONSUMER_NAME);
    }
}
