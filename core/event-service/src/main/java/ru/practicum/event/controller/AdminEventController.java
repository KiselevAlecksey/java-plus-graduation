package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.AdminGetEventRequestDto;
import ru.practicum.dto.EventFullResponseDto;

import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.validation.EventValidate;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService eventService;

    @RestLogging
    @GetMapping
    public List<EventFullResponseDto> adminGetEvents(@RequestBody @Valid AdminGetEventRequestDto requestParams) {
        int from = (requestParams.from() != null) ? requestParams.from() : 0;
        int size = (requestParams.size() != null) ? requestParams.size() : 10;
        return eventService.adminGetEvents(requestParams.toBuilder().from(from).size(size).build());
    }

    @RestLogging
    @PatchMapping("/{eventId}")
    public EventFullResponseDto adminChangeEvent(
            @PathVariable Long eventId,
            @RequestBody @Valid UpdateEventUserRequest eventDto
    ) {
        EventValidate.updateEventDateValidate(eventDto);
        EventValidate.textLengthValidate(eventDto);
        return eventService.adminChangeEvent(eventId, eventDto);
    }
}
