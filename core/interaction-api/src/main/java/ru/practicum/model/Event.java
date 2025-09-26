package ru.practicum.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

    Long id;

    Long initiator;

    String title;

    Category category;

    LocalDateTime eventDate;

    Location location;

    String annotation;

    String description;

    long participantLimit;

    Boolean paid;

    Boolean requestModeration;

    Long confirmedRequests;

    Double rating;

    LocalDateTime createdOn;

    LocalDateTime publishedOn;

    EventState state;

}
