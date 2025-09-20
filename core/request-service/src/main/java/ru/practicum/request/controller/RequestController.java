package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService service;

    @RestLogging
    @GetMapping
    public Collection<RequestDto> get(@PathVariable final long userId) {
        return service.getAllRequestByUserId(userId);
    }

    @RestLogging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto save(@PathVariable final long userId, @RequestParam long eventId) {
        return service.create(userId, eventId);
    }

    @RestLogging
    @PatchMapping("/{requestId}/cancel")
    public RequestDto delete(@PathVariable final long userId, @PathVariable long requestId) {
        return service.cancel(userId, requestId);
    }
}