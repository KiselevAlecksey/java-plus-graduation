package ru.practicum;

import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Builder
@Data
@AllArgsConstructor
public class Location {
    Float lat;
    Float lon;
}
