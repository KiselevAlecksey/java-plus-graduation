package ru.practicum.model;

import lombok.*;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {

    private Long id;

    private Long initiator;

    private String title;

    private Category category;

    private LocalDateTime eventDate;

    private Location location;

    private String annotation;

    private String description;

    private long participantLimit;

    private Boolean paid;

    private Boolean requestModeration;

    private Long confirmedRequests;

    private Long views;

    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;

    private EventState state;

}
