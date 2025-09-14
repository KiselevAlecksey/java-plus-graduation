package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.EventFullResponseDto;
import ru.practicum.event.service.EventService;
import ru.practicum.feign.UserEventControllerInternal;
import ru.practicum.validation.EventValidate;

@Slf4j
@RestController
@RequestMapping(path = "/internal/users/")
@RequiredArgsConstructor
public class UserEventControllerInternalImpl implements UserEventControllerInternal {
    private final EventService service;

    @GetMapping("{userId}/events/{id}")
    public EventFullResponseDto getEventById(@PathVariable Long userId, @PathVariable Long id) {
        return service.getEventById(userId, id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("{userId}/events")
    public EventFullResponseDto createEvent(@PathVariable Long userId,
                         @Valid @RequestBody NewEventDto event) {
        log.info("Попытка создания нового события {}", event);
        EventValidate.eventDateValidate(event);
        return service.createEvent(userId, event);
    }

    @GetMapping("{userId}/events")
    public EventFullResponseDto getEventByInitiatorId(@PathVariable Long userId) {
        return service.getEventByInitiatorId(userId);
    }
}