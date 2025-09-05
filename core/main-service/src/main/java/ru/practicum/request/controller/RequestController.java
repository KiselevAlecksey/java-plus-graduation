package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService service;

    @GetMapping
    public Collection<RequestDto> get(@PathVariable final long userId) {
        log.info("Get requests for user id {}", userId);
        Collection<RequestDto> response = service.getAllRequestByUserId(userId);
        log.info("Response Get requests for user id {}", response);
        return response;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto save(@PathVariable final long userId, @RequestParam long eventId) {
        log.info("Save request for user id {}, event id {}", userId, eventId);
        final RequestDto requestDto = service.create(userId, eventId);
        log.info("Saved request for user id and event id {}", requestDto);
        return requestDto;
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto delete(@PathVariable final long userId, @PathVariable long requestId) {
        log.info("Delete request for user id {}, request id {}", userId, requestId);
        RequestDto requestDto = service.cancel(userId, requestId);
        log.info("Deleted request for user id and request id {}", requestDto);
        return requestDto;
    }
}