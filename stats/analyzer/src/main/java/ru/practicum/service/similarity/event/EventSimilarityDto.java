package ru.practicum.service.similarity.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EventSimilarityDto {
    private Long eventId;

    private Double score;
}
