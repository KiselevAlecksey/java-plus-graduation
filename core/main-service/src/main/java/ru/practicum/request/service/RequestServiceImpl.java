package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.EventRepository;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.NotPossibleException;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.enums.RequestState;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public RequestDto create(long userId, long eventId) {
        if (!repository.findAllByRequesterIdAndEventIdAndStatusNotLike(userId, eventId,
                RequestState.CANCELED).isEmpty())
            throw new NotPossibleException("Request already exists");
        User user = getUser(userId);
        Event event = getEvent(eventId);
        if (userId == event.getInitiator().getId())
            throw new NotPossibleException("User is Initiator of event");
        if (!event.getState().equals(EventState.PUBLISHED))
            throw new NotPossibleException("Event is not published");
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit())
            throw new NotPossibleException("Request limit exceeded");
        Request newRequest = new Request();
        newRequest.setRequester(user);
        newRequest.setEvent(event);
        if (event.getRequestModeration() && event.getParticipantLimit() != 0) {
            newRequest.setStatus(RequestState.PENDING);
        } else {
            newRequest.setStatus(RequestState.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return requestMapper.toRequestDto(repository.save(newRequest));
    }

    @Override
    public List<RequestDto> getAllRequestByUserId(final long userId) {
        getUser(userId);
        return repository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public RequestDto cancel(final long userId, final long requestId) {
        getUser(userId);
        Request request = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Not found for request id" + requestId));
        if (!request.getRequester().getId().equals(userId))
            throw new NotPossibleException("Request is not by user");
        request.setStatus(RequestState.CANCELED);
        return requestMapper.toRequestDto(repository.save(request));
    }

    private Event getEvent(long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty())
            throw new NotFoundException("События с id = " + eventId + " не существует");
        return event.get();
    }

    private User getUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new NotFoundException("Пользователя с id = " + userId + " не существует");
        return user.get();
    }
}
