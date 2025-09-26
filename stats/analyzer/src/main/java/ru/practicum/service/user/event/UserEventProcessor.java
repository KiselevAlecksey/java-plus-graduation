package ru.practicum.service.user.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.EventHandler;
import ru.practicum.service.GenericEventProcessor;
import ru.practicum.service.client.Client;

import static ru.practicum.service.OffsetManager.manageOffsets;

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
