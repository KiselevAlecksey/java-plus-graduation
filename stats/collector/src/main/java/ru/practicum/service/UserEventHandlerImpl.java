package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class UserEventHandlerImpl implements UserEventHandler {
    private final KafkaEventProducer producer;
    private static final String USER_PRODUCER_NAME = "users";


    @Override
    public void handle(UserActionProto event) {

        Instant timestamp = event.hasTimestamp()
                ? Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos())
                : Instant.now();

        UserActionAvro userActionAvro = UserActionAvro.newBuilder()
                .setUserId(event.getUserId())
                .setEventId(event.getEventId())
                .setActionType(parseToActionAvro(event.getActionType()))
                .setTimestamp(timestamp)
                .build();

        sendHubEvent(userActionAvro);
    }

    private ActionTypeAvro parseToActionAvro(ActionTypeProto action) {
        return ActionTypeAvro.valueOf(Arrays.stream(action.name()
                        .split("_"))
                .reduce((first, second) -> second)
                .orElse(null));
    }

    private void sendHubEvent(UserActionAvro event) {
        producer.sendEvent(USER_PRODUCER_NAME, event);
    }
}
