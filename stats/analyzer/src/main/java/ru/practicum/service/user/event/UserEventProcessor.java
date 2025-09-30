package ru.practicum.service.user.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.service.EventHandler;
import ru.practicum.service.GenericEventProcessor;
import ru.practicum.service.client.Client;

@Slf4j
@Component
public class UserEventProcessor extends GenericEventProcessor {
    public UserEventProcessor(
            @Qualifier("interactionClient") Client userEventClient,
            @Qualifier("userEventHandler") EventHandler userEventHandler
    ) {
        super(userEventClient, userEventHandler, "USER_EVENT");
    }

}
