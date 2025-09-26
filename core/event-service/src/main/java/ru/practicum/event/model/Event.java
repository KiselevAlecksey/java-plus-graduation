package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.Category;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
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

    @Column(name = "title", length = 120, nullable = false)  // Добавлено length = 120
    String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @Column(name = "event_date", nullable = false)           // Добавлено name
    LocalDateTime eventDate;

    @Embedded
    Location location;

    @Column(name = "annotation", length = 2000, nullable = false)  // Добавлено length = 2000
    String annotation;

    @Column(name = "description", length = 7000, nullable = false) // Добавлено length = 7000
    String description;

    @Column(name = "participant_limit", nullable = false)
    long participantLimit;

    @Column(nullable = false)
    Boolean paid;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration;

    @Column(name = "confirmed_requests")
    Long confirmedRequests;

    @Column
    Double rating;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EventState state;
}
