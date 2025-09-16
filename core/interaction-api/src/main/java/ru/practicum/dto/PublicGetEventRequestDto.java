package ru.practicum.dto;

import lombok.Builder;
import ru.practicum.enums.EventSort;
import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record PublicGetEventRequestDto(
        String text,
        List<Long> categories,
        Boolean paid,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        Boolean onlyAvailable,
        EventSort sort,
        int from,
        int size
) {
}
