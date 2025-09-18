package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.EventFullResponseDto;
import ru.practicum.dto.PublicGetEventRequestDto;
import ru.practicum.enums.EventSort;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utils.Constant.FORMATTER;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {
    public static final String DEFAULT_SORT = "VIEWS";
    private final EventService eventService;

    @RestLogging
    @GetMapping
    public List<EventFullResponseDto> publicGetEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(defaultValue = "false") Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request
    ) {
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;

        if (start != null && end != null && (start.isAfter(end))) {
            throw new BadRequestException("Дата окончания, должна быть позже даты старта.");
        }

        return eventService.publicGetEvents(
                PublicGetEventRequestDto.builder()
                        .text(text)
                        .categories(categories)
                        .paid(paid)
                        .rangeStart(start)
                        .rangeEnd(end)
                        .onlyAvailable(onlyAvailable)
                        .sort(EventSort.valueOf(sort))
                        .from(from)
                        .size(size)
                        .build()
                , request);
    }

    @RestLogging
    @GetMapping("/{id}")
    EventFullResponseDto publicGetEvent(@PathVariable Long id, HttpServletRequest request) {
        return eventService.publicGetEvent(id, request);
    }
}