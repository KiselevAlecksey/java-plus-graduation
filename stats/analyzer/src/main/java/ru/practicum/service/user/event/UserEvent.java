package ru.practicum.service.user.event;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "interactions")
@IdClass(UserEventKey.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEvent {

    @Id
    @Column(name = "user_id")
    Long userId;

    @Id
    @Column(name = "event_id")
    Long eventId;

    @Column(name = "rating")
    double rating;

    @Column(name = "ts")
    Instant timestamp;
}
