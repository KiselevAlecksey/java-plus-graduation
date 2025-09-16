package ru.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullResponseDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.dto.UpdateEventUserRequestInteraction;

public interface UserEventControllerInternal {
    @GetMapping("/{userId}/events/{id}")
    EventFullResponseDto getEventById(@PathVariable Long userId, @PathVariable Long id);

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/events")
    EventFullResponseDto createEvent(@PathVariable Long userId,
                                     @Valid @RequestBody EventFullResponseDto event);

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/events/update")
    EventFullResponseDto updateEvent(@PathVariable Long userId,
                                     @Valid @RequestBody UpdateEventUserRequestInteraction event);

    @GetMapping("/{userId}/events")
    EventFullResponseDto getEventByInitiatorId(@PathVariable Long userId);
}
