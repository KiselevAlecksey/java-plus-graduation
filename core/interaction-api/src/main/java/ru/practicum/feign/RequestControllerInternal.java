package ru.practicum.feign;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.RequestDto;

import java.util.Collection;

public interface RequestControllerInternal {
    @GetMapping
    Collection<RequestDto> getRequests();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RequestDto save(@RequestBody RequestDto requestDto);
}

