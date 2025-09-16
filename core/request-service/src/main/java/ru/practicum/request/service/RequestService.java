package ru.practicum.request.service;

import ru.practicum.dto.RequestDto;

import java.util.List;

public interface RequestService {

    RequestDto getRequest(long requestId);

    RequestDto create(long userId, long eventId);

    List<RequestDto> getAllRequestByUserId(long userId);

    RequestDto cancel(long userId, long requestId);

    List<RequestDto> getAllRequests();

    RequestDto update(RequestDto requestDto);
}
