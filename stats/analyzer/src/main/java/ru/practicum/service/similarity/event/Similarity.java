package ru.practicum.service.similarity.event;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.service.user.event.UserEventKey;

import java.time.Instant;

@Entity
@Table(name = "similarities")
@IdClass(SimilarityEventKey.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Similarity {

    @Id
    @Column(name = "eventA")
    Long eventA;

    @Id
    @Column(name = "eventB")
    Long eventB;

    @Column(name = "similarity")
    double similarity;

    @Column(name = "ts")
    Instant timestamp;
}
