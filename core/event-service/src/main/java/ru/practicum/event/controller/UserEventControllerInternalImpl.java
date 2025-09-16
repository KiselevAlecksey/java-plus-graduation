package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.EventFullResponseDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.dto.UpdateEventUserRequestInteraction;
import ru.practicum.event.service.EventService;
import ru.practicum.feign.UserEventControllerInternal;
import ru.practicum.validation.EventValidate;

@Slf4j
@RestController
@RequestMapping(path = "/internal/users")
@RequiredArgsConstructor
public class UserEventControllerInternalImpl implements UserEventControllerInternal {
    private final EventService service;

    @Override
    public EventFullResponseDto getEventById(@PathVariable Long userId, @PathVariable Long id) {
        return service.getEventById(userId, id);
    }

    @Override
    public EventFullResponseDto createEvent(@PathVariable Long userId,
                         @Valid @RequestBody EventFullResponseDto event) {
        log.info("Попытка создания нового события {}", event);
        EventValidate.eventDateValidate(event);
        return service.createEvent(userId, event);
    }

    @Override
    public EventFullResponseDto updateEvent(Long userId, UpdateEventUserRequest event) {
        return service.updateEvent(userId, event);
    }

    @Override
    public EventFullResponseDto getEventByInitiatorId(@PathVariable Long userId) {
        return service.getEventByInitiatorId(userId);
    }
}