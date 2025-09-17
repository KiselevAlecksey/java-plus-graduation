package ru.practicum.feign;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.RequestDto;

import java.util.Collection;
import java.util.List;

public interface RequestControllerInternal {

    @GetMapping("/{requestId}")
    RequestDto getRequest(@PathVariable long requestId);

    @GetMapping("/all")
    Collection<RequestDto> getRequests();

    @GetMapping
    Collection<RequestDto> getRequestsByRequestIds(List<Long> requestIds);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RequestDto save(@RequestBody RequestDto requestDto);

    @PostMapping("/update")
    RequestDto update(@RequestBody RequestDto requestDto);
}

