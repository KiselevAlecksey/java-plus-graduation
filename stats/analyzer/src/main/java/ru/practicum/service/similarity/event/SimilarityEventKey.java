package ru.practicum.service.similarity.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimilarityEventKey implements Serializable {
    Long eventA;
    Long eventB;
}
