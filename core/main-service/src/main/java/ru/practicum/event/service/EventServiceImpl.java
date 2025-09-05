package ru.practicum.event.service;

import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatWebClient;
import ru.practicum.category.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.event.EventRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.dto.converter.EventToEventFullResponseDtoConverter;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.enums.StateAction;
import ru.practicum.event.model.Event;
import ru.practicum.exception.*;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.enums.RequestState;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    @Qualifier("mvcConversionService")
    private final ConversionService converter;
    private final EventToEventFullResponseDtoConverter listConverter;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String EVENT_NOT_FOUND_MESSAGE = "Event not found";
    private final StatWebClient statisticService;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<EventFullResponseDto> getEvents(Long userId, Integer from, Integer size) {
        getUser(userId);
        Pageable pageable = PageRequest.of(from, size);
        List<Event> respEvent = repository.findByInitiatorId(userId, pageable);
        return listConverter.convertList(respEvent);
    }

    @Override
    public EventFullResponseDto getEventById(Long userId, Long id, String ip, String uri) {
        getUser(userId);
        Event event = repository.findByIdAndInitiatorId(id, userId).orElseThrow(() ->
                new NotFoundException(EVENT_NOT_FOUND_MESSAGE));

        return converter.convert(event, EventFullResponseDto.class);
    }

    @Override
    public EventFullResponseDto createEvent(Long userId, NewEventDto eventDto) {
        User user = getUser(userId);
        Category category = getCategory(eventDto.category());
        Event event = converter.convert(eventDto, Event.class);
        if (event == null) throw new NoResultException("Не удалось создать событие");
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setConfirmedRequests(0L);
        event.setViews(0L);
        event = repository.save(event);
        event.setCreatedOn(LocalDateTime.now());
        return converter.convert(event, EventFullResponseDto.class);
    }

    @Override
    public EventFullResponseDto updateEvent(Long userId, UpdateEventUserRequest eventDto, Long eventId) {
        getUser(userId);
        Optional<Event> eventOptional = repository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
        }
        Event foundEvent = eventOptional.get();
        if (foundEvent.getState() == EventState.PUBLISHED) {
            throw new NotPossibleException("Нельзя изменять сообщение, которое опубликовано");
        }
        Event saved = repository.save(updateEventFields(eventDto, foundEvent));
        return converter.convert(saved, EventFullResponseDto.class);
    }

    @Override
    public List<EventFullResponseDto> publicGetEvents(String text,List<Long> categories, Boolean paid, String rangeStart,
                                                      String rangeEnd, Boolean onlyAvailable,String sort,Integer from,
                                                      Integer size, HttpServletRequest request) {

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;
        if (start != null && end != null) {
            if (start.isAfter(end))
                throw new BadRequestException("Дата окончания, должна быть позже даты старта.");
        }
        List<Event> events = repository.findEventsPublic(
                text,
                categories,
                paid,
                start,
                end,
                EventState.PUBLISHED,
                onlyAvailable,
                PageRequest.of(from,
                        size)
        );

        hit(request.getRemoteAddr(), request.getRequestURI());

        return listConverter.convertList(events);
    }

    @Override
    public EventFullResponseDto publicGetEvent(Long id, HttpServletRequest request) {
        Event event = getEvent(id);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие c ID: " + id + " не найдено");
        }
        hit(request.getRemoteAddr(),request.getRequestURI());

        Long views = statisticService.getEventViews(request.getRequestURI());
        if (views != null) {
            event.setViews(views);
        }
        event = repository.save(event);
        return converter.convert(event, EventFullResponseDto.class);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        getUser(userId);
        Event event = getEvent(eventId);

        List<RequestDto> confirmedReqs = new ArrayList<>();
        List<RequestDto> canceledReqs = new ArrayList<>();

        for (Long requestId : request.getRequestIds()) {
            Request newRequest = requestRepository
              .findById(requestId).orElseThrow(() -> new NotFoundException("Запрос c ID: " + requestId + " не найден"));
            processRequestStatus(request.getStatus(), event, newRequest, confirmedReqs, canceledReqs);
        }

        repository.save(event);
        return new EventRequestStatusUpdateResult(confirmedReqs, canceledReqs);
    }

    @Override
    public List<RequestDto> getUserRequests(Long userId, Long eventId) {
        userRepository.findById(userId);
        Event event = repository.findById(eventId).orElse(new Event());
        List<Request> requests = requestRepository.findAllByEvent(event);
        if (requests.isEmpty()) return new ArrayList<>();
        return requests.stream().map(requestMapper::toRequestDto).collect(Collectors.toList());
    }

    @Override
    public Event getEventByInitiator(Long userId) {
        return repository.findByInitiatorId(userId);
    }

    private void processRequestStatus(String status, Event event, Request request,
                                      List<RequestDto> confirmedReqs,
                                      List<RequestDto> canceledReqs) {
        if (!request.getStatus().equals(RequestState.PENDING)) {
            if (request.getStatus().equals(RequestState.CONFIRMED)) {
                throw new NotPossibleException("Request already confirmed");
            } else {
                throw new BadRequestException("Request " + request.getId() + " is not pending");
            }
        } else {
            if (status.equals("CONFIRMED")) {
                confirmedStatus(event, request, confirmedReqs);
            } else {
                request.setStatus(RequestState.REJECTED);
                canceledReqs.add(requestMapper.toRequestDto(request));
                requestRepository.save(request);
            }
        }
    }

    private void confirmedStatus(Event event, Request request,
                                       List<RequestDto> confirmedRequests) {
        if (event.getConfirmedRequests() >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            request.setStatus(RequestState.CANCELED);
            confirmedRequests.add(requestMapper.toRequestDto(request));
            requestRepository.save(request);
            throw new NotPossibleException("The participant limit is reached");
        }
        request.setStatus(RequestState.CONFIRMED);
        event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        confirmedRequests.add(requestMapper.toRequestDto(request));
        requestRepository.save(request);
    }

    @Override
    public List<EventFullResponseDto> adminGetEvents(AdminGetEventRequestDto requestParams) {
        int from = (requestParams.from() != null) ? requestParams.from() : 0;
        int size = (requestParams.size() != null) ? requestParams.size() : 10;
        LocalDateTime startTime = requestParams.rangeStart() != null ? LocalDateTime.parse(requestParams.rangeStart(), FORMATTER) : null;
        LocalDateTime endTime = requestParams.rangeEnd() != null ? LocalDateTime.parse(requestParams.rangeEnd(), FORMATTER) : null;
        List<Event> events = repository.findEventsByAdmin(
                requestParams.users(),
                requestParams.states(),
                requestParams.categories(),
                startTime,
                endTime,
                PageRequest.of(from / size,
                        size)
        );
        return listConverter.convertList(events);
    }

    @Override
    public EventFullResponseDto adminChangeEvent(Long eventId, UpdateEventUserRequest eventDto) {
        Event event = getEvent(eventId);
        checkEventForUpdate(event, eventDto.stateAction());
        Event updatedEvent = repository.save(prepareEventForUpdate(event, eventDto));
        return converter.convert(updatedEvent, EventFullResponseDto.class);
    }

    private Event getEvent(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND_MESSAGE));
    }

    private void checkEventForUpdate(Event event, StateAction action) {
        checkEventDate(event.getEventDate());
        if (action == null) return;
        if (action.equals(StateAction.PUBLISH_EVENT)
                && !event.getState().equals(EventState.PENDING))
            throw new ParameterConflictException("Events", "Опубликовать событие можно в статусе PENDING, а статус = "
                                                           + event.getState());
        if (action.equals(StateAction.REJECT_EVENT)
                && event.getState().equals(EventState.PUBLISHED))
            throw new ParameterConflictException("Events", "Отменить событие можно только в статусе PUBLISHED, а статус = "
                    + event.getState());
    }

    private Event prepareEventForUpdate(Event event, UpdateEventUserRequest updateEventDto) {
        if (updateEventDto.annotation() != null)
            event.setAnnotation(updateEventDto.annotation());
        if (updateEventDto.description() != null)
            event.setDescription(updateEventDto.description());
        if (updateEventDto.eventDate() != null) {
            checkEventDate(updateEventDto.eventDate());
            event.setEventDate(updateEventDto.eventDate());
        }
        if (updateEventDto.paid() != null)
            event.setPaid(updateEventDto.paid());
        if (updateEventDto.participantLimit() != null)
            event.setParticipantLimit(updateEventDto.participantLimit());
        if (updateEventDto.title() != null)
            event.setTitle(updateEventDto.title());
        if (updateEventDto.stateAction() != null) {
            switch (updateEventDto.stateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case CANCEL_REVIEW, REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
            }
        }
        return event;
    }

    private void checkEventDate(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now().plusHours(1)))
            throw new ConflictException("Дата начала события меньше чем час " + dateTime);
    }

    private User getUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь c ID: " + userId + " не найден");
        }
        return user.get();
    }

    private Category getCategory(Long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isEmpty()) {
            throw new NotFoundException("Категория c ID: " + categoryId + " не найдена");
        }
        return category.get();
    }


    private Event updateEventFields(UpdateEventUserRequest eventDto, Event foundEvent) {
        if (eventDto.category() != null) {
            Category category = getCategory(eventDto.category());
            foundEvent.setCategory(category);
        }

        if (eventDto.annotation() != null && !eventDto.annotation().isBlank()) {
            foundEvent.setAnnotation(eventDto.annotation());
        }
        if (eventDto.description() != null && !eventDto.description().isBlank()) {
            foundEvent.setDescription(eventDto.description());
        }
        if (eventDto.eventDate() != null) {
            if (eventDto.eventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Дата начала события не может быть раньше чем через 2 часа");
            }
            foundEvent.setEventDate(eventDto.eventDate());
        }
        if (eventDto.paid() != null) {
            foundEvent.setPaid(eventDto.paid());
        }
        if (eventDto.participantLimit() != null) {
            if (eventDto.participantLimit() < 0) {
                throw new ValidationException("Participant limit cannot be negative");
            }
            foundEvent.setParticipantLimit(eventDto.participantLimit());
        }
        if (eventDto.requestModeration() != null) {
            foundEvent.setRequestModeration(eventDto.requestModeration());
        }
        if (eventDto.title() != null && !eventDto.title().isBlank()) {
            foundEvent.setTitle(eventDto.title());
        }
        if (eventDto.location() != null) {
            if (eventDto.location().getLat() != null) {
                foundEvent.getLocation().setLat(eventDto.location().getLat());
            }
            if (eventDto.location().getLon() != null) {
                foundEvent.getLocation().setLon(eventDto.location().getLon());
            }
        }

        if (eventDto.stateAction() != null) {
            switch (eventDto.stateAction()) {
                case CANCEL_REVIEW -> foundEvent.setState(EventState.CANCELED);
                case PUBLISH_EVENT -> foundEvent.setState(EventState.PUBLISHED);
                case SEND_TO_REVIEW -> foundEvent.setState(EventState.PENDING);
            }
        }
        return foundEvent;
    }


    private List<String> getListOfUri(List<Event> events, String uri) {
        return events.stream().map(Event::getId).map(id -> getUriForEvent(uri, id))
                .collect(Collectors.toList());
    }

    private String getUriForEvent(String uri, Long eventId) {
        return uri + "/" + eventId;
    }

    private void hit(String ip, String uri) {
        HitDto hit = new HitDto("ewm-main", uri, ip, LocalDateTime.now());
        statisticService.addHit(hit);
    }
}
