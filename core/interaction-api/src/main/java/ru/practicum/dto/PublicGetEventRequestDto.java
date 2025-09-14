package ru.practicum.dto;

import ru.practicum.enums.EventSort;
import java.time.LocalDateTime;
import java.util.List;

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
