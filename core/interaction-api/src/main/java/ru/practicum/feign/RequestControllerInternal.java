package ru.practicum.feign;

import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.RequestDto;

import java.util.Collection;
import java.util.List;

public interface RequestControllerInternal {

    @GetMapping("/{requestId}")
    RequestDto getRequest(@PathVariable @Positive long requestId);

    @GetMapping("/all")
    Collection<RequestDto> getRequests();

    @PostMapping("/by-ids")
    Collection<RequestDto> getRequestsByRequestIds(@RequestBody List<Long> requestIds);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RequestDto save(@RequestBody RequestDto requestDto);

    @PostMapping("/update")
    RequestDto update(@RequestBody RequestDto requestDto);

    @GetMapping("/users/{userId}/events/{eventId}/check")
    boolean checkRequestConfirmed(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId
    );
}

