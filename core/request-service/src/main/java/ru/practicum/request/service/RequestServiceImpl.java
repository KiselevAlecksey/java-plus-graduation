package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.UserDto;
import ru.practicum.enums.EventState;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.NotPossibleException;
import ru.practicum.feign.EventFeignClient;
import ru.practicum.feign.UserFeignClient;
import ru.practicum.mapper.EventMapperInteraction;
import ru.practicum.mapper.UserMapperInteraction;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.request.enums.RequestState;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserFeignClient userClient;
    private final EventFeignClient eventClient;
    private final RequestMapper requestMapper;
    private final UserMapperInteraction userMapper;
    private final EventMapperInteraction eventMapper;
    private Map<Long, User> userMap = new HashMap<>();

    private Map<Long, User> getUserMap() {
        return userMap = new HashMap<>(userClient.getUsers().stream()
                .collect(Collectors.toMap(UserDto::id, userMapper::toUser)));
    }

    private Optional<User> getUserFromMap(Long userId) {
        return userMap.isEmpty() ? Optional.empty() : Optional.of(userMap.get(userId));
    }

    @Override
    public RequestDto create(long userId, long eventId) {
        if (!requestRepository.findAllByRequesterAndEventAndStatusNotLike(userId, eventId,
                RequestState.CANCELED).isEmpty())
            throw new NotPossibleException("Request already exists");
        checkUserMapContainsValue(userId);
        Event event = getEvent(userId, eventId);
        if (userId == event.getInitiator())
            throw new NotPossibleException("User is Initiator of event");
        if (!event.getState().equals(EventState.PUBLISHED))
            throw new NotPossibleException("Event is not published");
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit())
            throw new NotPossibleException("Request limit exceeded");
        Request newRequest = new Request();
        newRequest.setRequester(userId);
        newRequest.setEvent(eventId);
        if (event.getRequestModeration() && event.getParticipantLimit() != 0) {
            newRequest.setStatus(RequestState.PENDING);
        } else {
            newRequest.setStatus(RequestState.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventClient.createEvent(userId, eventMapper.toNewEventDto(event));
        }
        return requestMapper.toRequestDto(requestRepository.save(newRequest));
    }

    @Override
    public List<RequestDto> getAllRequestByUserId(final long userId) {
        checkUserMapContainsValue(userId);
        return requestRepository.findAllByRequester(userId).stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    public List<RequestDto> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public RequestDto cancel(final long userId, final long requestId) {
        checkUserMapContainsValue(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Not found for request id" + requestId));
        if (!request.getRequester().equals(userId))
            throw new NotPossibleException("Request is not by user");
        request.setStatus(RequestState.CANCELED);
        return requestMapper.toRequestDto(requestRepository.save(request));
    }

    private Event getEvent(long userId, long eventId) {
        return Optional.of(eventMapper.toEventFromEventFullResponseDto(eventClient.getEventById(userId, eventId))).orElseThrow(
                () -> new NotFoundException("События с id = " + eventId + " не существует")
        );
    }

    private void checkUserMapContainsValue(Long userId) {
        Optional<User> userOptional = getUserFromMap(userId);
        if (userOptional.isEmpty()) {
            getUserMap();
        }
        if (userMap.get(userId) == null) {
            throw new NotFoundException("Пользователь c ID: " + userId + " не найден");
        }
    }
}
