package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.validation.EventValidate;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class UserEventController {
    private final EventService service;

    @RestLogging
    @GetMapping
    List<EventFullResponseDto> getEvents(
            @PathVariable Long userId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size
    ) {
        return service.getEvents(userId, from, size);
    }

    @RestLogging
    @GetMapping("/{id}")
    EventFullResponseDto getEvent(
            @PathVariable Long userId,
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        return service.getEventById(userId, id, ip, uri);
    }

    @RestLogging
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    EventFullResponseDto createEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto event
    ) {
        EventValidate.eventDateValidate(event);
        return service.createEvent(userId, event);
    }

    @RestLogging
    @PatchMapping("/{eventId}")
    EventFullResponseDto updateEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest event
    ) {
        EventValidate.updateEventDateValidate(event);
        EventValidate.textLengthValidate(event);
        return service.updateEvent(userId, event, eventId);
    }

    @RestLogging
    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequest(
            @PathVariable Long userId, @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest request
    ) {
        log.info("Попытка изменить статуса заявок на участие в событии c ID: {} от пользователя с ID: {}", eventId, userId);
        return service.updateRequestStatus(userId, eventId, request);
    }

    @RestLogging
    @GetMapping("/{eventId}/requests")
    public List<RequestDto> getUserRequests(@PathVariable Long userId, @PathVariable Long eventId) {
    return service.getUserRequests(userId, eventId);
    }

}