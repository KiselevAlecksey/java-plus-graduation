package ru.practicum.service.user.event;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.EventHandler;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Qualifier("userEventHandler")
public class UserEventHandlerImpl implements EventHandler {
    private final UserEventRepository userEventRepository;

    @Value("${ratings.view:0.4}")
    private double viewRating;

    @Value("${ratings.register:0.8}")
    private double registerRating;

    @Value("${ratings.like:1.0}")
    private double likeRating;

    @Override
    @Transactional
    public void handle(SpecificRecord event) {
        UserEvent userEvent = toUserEvent((UserActionAvro) event);
        Optional<UserEvent> userEventOptional = userEventRepository.findUserEventByEventIdAndUserId(
                userEvent.getEventId(),
                userEvent.getUserId()
        );

        if (userEventOptional.isPresent()
                && userEvent.getRating() > userEventOptional.get().getRating()) {
            userEvent.setRating(userEventOptional.get().getRating());
        }

        userEventRepository.save(userEvent);
    }

    private UserEvent toUserEvent(UserActionAvro event) {
        return UserEvent.builder()
                .userId(event.getUserId())
                .eventId(event.getEventId())
                .rating(getRating(event.getActionType()))
                .timestamp(event.getTimestamp())
                .build();
    }

    private double getRating(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> viewRating;
            case REGISTER -> registerRating;
            case LIKE -> likeRating;
            default -> throw new IllegalArgumentException("Unavailable action type: " + type);
        };
    }

}
