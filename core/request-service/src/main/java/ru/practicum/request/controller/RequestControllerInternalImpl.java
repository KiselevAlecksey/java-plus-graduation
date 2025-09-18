package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.RequestDto;
import ru.practicum.feign.RequestControllerInternal;
import ru.practicum.request.service.RequestService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal/users/requests")
@RequiredArgsConstructor
public class RequestControllerInternalImpl implements RequestControllerInternal {
    private final RequestService service;

    @Override
    public RequestDto getRequest(long requestId) {
        return service.getRequest(requestId);
    }

    @Override
    public Collection<RequestDto> getRequests() {
        return service.getAllRequestsByIds();
    }

    @Override
    public Collection<RequestDto> getRequestsByRequestIds(List<Long> requestIds) {
        return service.getAllRequestsByIds(requestIds);
    }

    @Override
    public RequestDto save(RequestDto requestDto) {
        return service.create(requestDto.requester(), requestDto.event());
    }

    @Override
    public RequestDto update(RequestDto requestDto) {
        return service.update(requestDto);
    }
}