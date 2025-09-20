package ru.practicum.dto;

import ru.practicum.enums.StateAction;
import ru.practicum.model.Location;

import java.time.LocalDateTime;

public interface EventUpdateRequest {
    Long id();
    String title();
    Long category();
    LocalDateTime eventDate();
    Location location();
    String annotation();
    String description();
    Long participantLimit();
    Boolean paid();
    Boolean requestModeration();
    StateAction stateAction();
}
