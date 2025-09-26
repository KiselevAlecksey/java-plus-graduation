package ru.practicum.service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;

@Component("interactionClient")
@Qualifier("interactionClient")
public class UserEventClient extends AbstractBaseClient {
    private static final String CONSUMER_USERS_NAME = "interaction";

    public UserEventClient(KafkaConfig config) {
        super(config, CONSUMER_USERS_NAME);
    }
}
