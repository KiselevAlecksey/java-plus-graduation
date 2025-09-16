package ru.practicum.dto;

import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record AdminGetEventRequestDto(
        List<Long> users,
        List<EventState> states,
        List<Long> categories,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeStart,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeEnd,
        Integer from,
        Integer size
) {}
