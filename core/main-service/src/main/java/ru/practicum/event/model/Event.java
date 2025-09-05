package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.category.model.Category;
import ru.practicum.event.enums.EventState;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@ToString
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private User initiator;

    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    private Category category;

    private LocalDateTime eventDate;

    @Embedded
    private Location location;

    private String annotation;

    private String description;

    private long participantLimit;

    private Boolean paid;

    private Boolean requestModeration;

    @Column(name = "confirmed_requests")
    private Long confirmedRequests;

    @Column(name = "views")
    private Long views;

    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;

    @Enumerated(EnumType.STRING)
    private EventState state;

}
