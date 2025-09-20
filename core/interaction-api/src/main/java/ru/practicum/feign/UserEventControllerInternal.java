package ru.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.EventFullResponseDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.dto.UpdateEventUserRequestInteraction;

public interface UserEventControllerInternal {

    @RestLogging
    @GetMapping("/{userId}/events/{id}")
    EventFullResponseDto getEventById(@PathVariable Long userId, @PathVariable Long id);

    @RestLogging
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/events")
    EventFullResponseDto createEvent(@PathVariable Long userId, @Valid @RequestBody EventFullResponseDto event);

    @RestLogging
    @PostMapping("/{userId}/events/update")
    EventFullResponseDto updateEvent(@PathVariable Long userId, @Valid @RequestBody UpdateEventUserRequest event);

    @RestLogging
    @GetMapping("/{userId}/events")
    EventFullResponseDto getEventByInitiatorId(@PathVariable Long userId);
}
