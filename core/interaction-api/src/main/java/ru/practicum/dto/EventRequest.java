package ru.practicum.dto;

import ru.practicum.model.Location;

import java.time.LocalDateTime;

public interface EventRequest {
    String title();

    LocalDateTime eventDate();

    Location location();

    String annotation();

    String description();

    Long participantLimit();

    Boolean paid();

    Boolean requestModeration();
}
