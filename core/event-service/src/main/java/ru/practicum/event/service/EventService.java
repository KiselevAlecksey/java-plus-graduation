package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.*;
import ru.practicum.dto.EventFullResponseDto;

import java.util.List;

public interface EventService {
    List<EventFullResponseDto> getEvents(Long userId, Integer from, Integer size);

    EventFullResponseDto getEventById(Long userId, Long eventId, String ip, String uri);

    EventFullResponseDto getEventById(Long userId, Long id);

    EventFullResponseDto createEvent(Long userId, NewEventDto eventDto);

    EventFullResponseDto createEvent(Long userId, EventFullResponseDto eventDto);

    List<EventFullResponseDto> adminGetEvents(AdminGetEventRequestDto requestParams);

    EventFullResponseDto adminChangeEvent(Long eventId, UpdateEventUserRequest eventDto);

    EventFullResponseDto updateEvent(Long userId, UpdateEventUserRequest eventDto, Long eventId);

    EventFullResponseDto updateEvent(Long userId, UpdateEventUserRequest eventDto);

    List<EventFullResponseDto> publicGetEvents(String text,List<Long> categories, Boolean paid, String rangeStart,
                                               String rangeEnd, Boolean onlyAvailable,String sort,Integer from,
                                               Integer size, HttpServletRequest request);

    EventFullResponseDto publicGetEvent(Long eventId, HttpServletRequest request);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<RequestDto> getUserRequests(Long userId, Long eventId);

    EventFullResponseDto getEventByInitiatorId(Long userId);
}
