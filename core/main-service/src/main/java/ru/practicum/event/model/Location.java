package ru.practicum.event.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Builder
@Embeddable
@Data
@AllArgsConstructor
public class Location {
    Float lat;
    Float lon;
}
