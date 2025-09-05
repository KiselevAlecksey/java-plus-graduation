package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.event.validation.EventValidate;
import ru.practicum.request.dto.RequestDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class UserEventController {
    private final EventService service;

    @GetMapping
    List<EventFullResponseDto> getEvents(@PathVariable Long userId,
                                         @RequestParam(name = "from", defaultValue = "0") Integer from,
                                         @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return service.getEvents(userId, from, size);
    }

    @GetMapping("/{id}")
    EventFullResponseDto getEvent(@PathVariable Long userId,
                      @PathVariable Long id,
                      HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        return service.getEventById(userId, id, ip, uri);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    EventFullResponseDto createEvent(@PathVariable Long userId,
                         @Valid @RequestBody NewEventDto event) {
        log.info("Попытка создания нового события {}", event);
        EventValidate.eventDateValidate(event);
        return service.createEvent(userId, event);
    }

    @PatchMapping("/{eventId}")
    EventFullResponseDto updateEvent(@PathVariable Long userId,
                         @PathVariable Long eventId,
                         @Valid @RequestBody UpdateEventUserRequest event) {
        EventValidate.updateEventDateValidate(event);
        EventValidate.textLengthValidate(event);
        return service.updateEvent(userId, event, eventId);
    }

    @PatchMapping("{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequest(@PathVariable Long userId, @PathVariable Long eventId,
                                                        @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Попытка изменить статуса заявок на участие в событии c ID: {} от пользователя с ID: {}", eventId, userId);
        return service.updateRequestStatus(userId, eventId, request);
    }

    @GetMapping("{eventId}/requests")
    public List<RequestDto> getUserRequests(@PathVariable Long userId, @PathVariable Long eventId) {
    log.info("Получение запросов на участие в событии с ID: {} пользователя с ID: {}", eventId, userId);
    return service.getUserRequests(userId, eventId);
    }

}