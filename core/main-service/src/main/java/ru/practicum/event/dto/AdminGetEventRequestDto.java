package ru.practicum.event.dto;

import lombok.Builder;
import ru.practicum.event.enums.EventState;

import java.util.List;

@Builder(toBuilder = true)
public record AdminGetEventRequestDto(
        List<Long> users,
        List<EventState> states,
        List<Long> categories,
        String rangeStart,
        String rangeEnd,
        Integer from,
        Integer size
) {
}
