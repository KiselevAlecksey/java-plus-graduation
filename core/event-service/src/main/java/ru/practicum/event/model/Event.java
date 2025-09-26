package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.Category;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "initiator_id", nullable = false)
    Long initiator;

    String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    LocalDateTime eventDate;

    @Embedded
    Location location;

    String annotation;

    String description;

    long participantLimit;

    Boolean paid;

    Boolean requestModeration;

    @Column(name = "confirmed_requests")
    Long confirmedRequests;

    @Column(name = "rating")
    Double rating;

    LocalDateTime createdOn;

    LocalDateTime publishedOn;

    @Enumerated(EnumType.STRING)
    EventState state;
}
