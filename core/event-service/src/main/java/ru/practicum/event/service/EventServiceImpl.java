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
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.AnalyzerGrpcClient;
import ru.practicum.CollectorGrpcClient;
import ru.practicum.category.CategoryRepository;
import ru.practicum.category.Category;
import ru.practicum.dto.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestState;
import ru.practicum.enums.StateAction;
import ru.practicum.event.EventRepository;
import ru.practicum.dto.EventFullResponseDto;
import ru.practicum.dto.converter.EventToEventFullResponseDtoConverterInteraction;
import ru.practicum.event.dto.converter.NewEventDtoToEvent;
import ru.practicum.event.model.Event;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.mapper.RequestMapperInteraction;
import ru.practicum.mapper.UserMapperInteraction;
import ru.practicum.exception.*;
import ru.practicum.feign.RequestFeignClient;
import ru.practicum.feign.UserFeignClient;
import ru.practicum.model.Location;
import ru.practicum.model.Request;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.enums.StateAction.*;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    public static final String VIEW = "ACTION_VIEW";
    public static final String LIKE = "ACTION_LIKE";
    private Map<Long, User> userMap = new HashMap<>();
    private Map<Long, Request> requestMap = new HashMap<>();
    private final UserMapperInteraction userMapper;
    private final EventRepository eventRepository;
    private final UserFeignClient userClient;
    private final CategoryRepository categoryRepository;
    private final NewEventDtoToEvent newEventDtoToEvent;
    @Qualifier("mvcConversionService")
    private final ConversionService converter;
    private final EventToEventFullResponseDtoConverterInteraction listConverter;
    private static final String EVENT_NOT_FOUND_MESSAGE = "Event not found";
    private final AnalyzerGrpcClient analyzerGrpcClient;
    private final CollectorGrpcClient collectorGrpcClient;
    private final RequestFeignClient requestClient;
    private final RequestMapperInteraction requestMapper;

    private void checkUserMapContainsValue(Long userId) {
        if (getUserFromMap(userId).isEmpty()) {
            getUserMapFromUserService();
        }
        getUserFromMap(userId).orElseThrow(() -> new NotFoundException("Пользователь c ID: " + userId + " не найден"));
    }

    private void getUserMapFromUserService() {
        userMap = new HashMap<>(userClient.getUsers().stream()
                    .collect(Collectors.toMap(UserDto::id, userMapper::toUser)));
    }

    private Optional<User> getUserFromMap(Long userId) {
        return Optional.ofNullable(userMap.getOrDefault(userId, null));
    }

    private Optional<Request> getRequestFromMap(Long requestId) {
        return Optional.ofNullable(requestMap.getOrDefault(requestId, null));
    }

    private Map<Long, Request> getRequestMap() {
        return requestMap = new HashMap<>(requestClient.getRequests().stream()
                .collect(Collectors.toMap(RequestDto::id, requestMapper::toRequest)));
    }

    private List<Request> getRequestListFromMap(Long eventId) {
        List<Request> requests = getRequestMap().values().stream()
                .filter(request -> request.getEvent().equals(eventId))
                .toList();

        return requests.isEmpty() ? List.of() : requests;
    }

    private Collection<RequestDto> getRequestDtoCollectionFromRequestService(List<Long> requestIds) {
        Collection<RequestDto> requestDtos = requestClient.getRequestsByRequestIds(requestIds);

        requestMap.putAll(new HashMap<>(requestDtos.stream()
                .collect(Collectors.toMap(RequestDto::id, requestMapper::toRequest))));
        return requestDtos;
    }

    private Request getRequestOrGetFromRequestService(Long requestId) {
        if (getRequestFromMap(requestId).isEmpty()) {
            getRequestMap();
        }

        return getRequestFromMap(requestId).orElseThrow(
                () -> new NotFoundException("Запрос c ID: " + requestId + " не найден"));
    }

    @Override
    public List<EventFullResponseDto> getEvents(Long userId, Integer from, Integer size) {
        checkUserMapContainsValue(userId);
        Pageable pageable = PageRequest.of(from, size);
        List<Event> respEvent = eventRepository.findByInitiator(userId, pageable);
        return listConverter.convertList(respEvent.stream()
                .map(getEventEventFunction())
                .toList()
        );
    }

    @Override
    public EventFullResponseDto getEventById(Long userId, Long id, String ip, String uri) {
        checkUserMapContainsValue(userId);
        Event event = eventRepository.findByIdAndInitiator(id, userId).orElseThrow(() ->
                new NotFoundException(EVENT_NOT_FOUND_MESSAGE));

        return converter.convert(event, EventFullResponseDto.class);
    }

    @Override
    public EventFullResponseDto getEventById(Long userId, Long id) {
        checkUserMapContainsValue(userId);
        Event event = eventRepository.findById(id).orElseThrow(
                () -> new NotFoundException(EVENT_NOT_FOUND_MESSAGE));

        return converter.convert(event, EventFullResponseDto.class);
    }

    @Override
    public EventFullResponseDto createEvent(Long userId, NewEventDto eventDto) {
        checkUserMapContainsValue(userId);
        Category category = getCategory(eventDto.category());
        Event event = newEventDtoToEvent.convert(eventDto);
        if (event == null) throw new NoResultException("Не удалось создать событие");
        event.setInitiator(userId);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setConfirmedRequests(0L);
        event.setRating(0.0);
        event = eventRepository.save(event);
        event.setCreatedOn(LocalDateTime.now());
        return converter.convert(event, EventFullResponseDto.class);
    }

    @Override
    public EventFullResponseDto createEvent(Long userId, EventFullResponseDto eventDto) {
        checkUserMapContainsValue(userId);
        Category category = getCategory(eventDto.category().id());
        Event event = Event.builder()
                .annotation(eventDto.annotation())
                .createdOn(eventDto.createdOn())
                .description(eventDto.description())
                .eventDate(eventDto.eventDate())
                .paid(eventDto.paid())
                .participantLimit(eventDto.participantLimit())
                .confirmedRequests(eventDto.confirmedRequests())
                .requestModeration(eventDto.requestModeration())
                .title(eventDto.title())
                .location(new ru.practicum.event.model.Location(
                        eventDto.location().getLat(), eventDto.location().getLon()))
                .build();
        if (event == null) throw new NoResultException("Не удалось создать событие");
        event.setInitiator(userId);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setRating(0.0);
        event = eventRepository.save(event);
        event.setCreatedOn(LocalDateTime.now());

        return converter.convert(event, EventFullResponseDto.class);
    }

    @Override
    public EventFullResponseDto updateEvent(Long userId, UpdateEventUserRequest eventDto) {
        checkUserMapContainsValue(userId);
        Event event = eventRepository.findById(eventDto.id()).orElseThrow(() ->
                new NotFoundException(EVENT_NOT_FOUND_MESSAGE));

        Event saved = eventRepository.save(updateEventFields(eventDto, event));
        return converter.convert(saved, EventFullResponseDto.class);
    }

    @Override
    public EventFullResponseDto updateEvent(Long userId, UpdateEventUserRequest eventDto, Long eventId) {
        checkUserMapContainsValue(userId);
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
        }
        Event foundEvent = eventOptional.get();
        if (foundEvent.getState() == EventState.PUBLISHED) {
            throw new NotPossibleException("Нельзя изменять сообщение, которое опубликовано");
        }
        Event saved = eventRepository.save(updateEventFields(eventDto, foundEvent));
        return converter.convert(saved, EventFullResponseDto.class);
    }

    @Override
    public List<EventFullResponseDto> publicGetEvents(
            PublicGetEventRequestDto dto, HttpServletRequest request
    ) {
        List<Event> events = eventRepository.findEventsPublic(
                dto.text(),
                dto.categories(),
                dto.paid(),
                dto.rangeStart(),
                dto.rangeEnd(),
                EventState.PUBLISHED,
                dto.onlyAvailable(),
                PageRequest.of(dto.from(),
                        dto.size())
        );

        return listConverter.convertList(events.stream()
                .map(getEventEventFunction())
                .toList()
        );
    }

    private static Function<Event, ru.practicum.model.Event> getEventEventFunction() {
        return event -> ru.practicum.model.Event.builder()
                .id(event.getId())
                .initiator(event.getInitiator())
                .title(event.getTitle())
                .category(new ru.practicum.model.Category(event.getCategory().getId(), event.getCategory().getName()))
                .eventDate(event.getEventDate())
                .location(new Location(event.getLocation().getLat(), event.getLocation().getLon()))
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .participantLimit(event.getParticipantLimit())
                .paid(event.getPaid())
                .requestModeration(event.getRequestModeration())
                .confirmedRequests(event.getConfirmedRequests())
                .rating(event.getRating())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .state(event.getState())
                .build();
    }

    @Override
    public EventFullResponseDto publicGetEvent(Long eventId, Long userId, HttpServletRequest request) {
        Event event = getEvent(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие c ID: " + eventId + " не найдено");
        }
        collectorGrpcClient.collectUserAction(userId, eventId, VIEW);
        Double rating = analyzerGrpcClient.getInteractionsCount(List.of(eventId)).toList().getFirst().getScore();

        event.setRating(rating);

        event = eventRepository.save(event);
        return converter.convert(event, EventFullResponseDto.class);
    }

    @Override
    public List<RequestDto> getUserRequests(Long userId, Long eventId) {
        checkUserMapContainsValue(userId);
        eventRepository.existsById(eventId);
        return getRequestListFromMap(eventId).stream().map(requestMapper::toRequestDto).toList();
    }

    @Override
    public EventFullResponseDto getEventByInitiatorId(Long userId) {
        return converter.convert(eventRepository.findByInitiator(userId), EventFullResponseDto.class);
    }

    @Override
    public void putLike(Long userId, Long eventId) {
        if (!requestClient.checkRequestConfirmed(userId, eventId)) {
            throw new ConditionsNotMetException("Пользователь может лайкать только посещённые им мероприятия");
        }

        collectorGrpcClient.collectUserAction(userId, eventId, LIKE);
    }

    @Override
    public List<EventFullResponseDto> publicGetRecommendations(Long userId, Integer maxResult) {
        Map<Long, Double> eventScoreMap = analyzerGrpcClient.getRecommendationsForUser(userId, maxResult).parallel()
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));

        List<Event> events = eventRepository.findAllByIdIn(eventScoreMap.keySet());

        return events.parallelStream()
                .map(event -> {
                    event.toBuilder().rating((eventScoreMap.getOrDefault(event.getId(), 0.0)));
                    return converter.convert(event, EventFullResponseDto.class);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(EventFullResponseDto::rating).reversed())
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(
            Long userId, Long eventId,
            EventRequestStatusUpdateRequest request
    ) {
        checkUserMapContainsValue(userId);
        Event event = getEvent(eventId);

        List<RequestDto> confirmedReqs = new ArrayList<>();
        List<RequestDto> canceledReqs = new ArrayList<>();

        for (RequestDto requestDto : getRequestDtoCollectionFromRequestService(request.getRequestIds())) {

            if (!requestDto.status().equals(RequestState.PENDING)) {
                if (requestDto.status().equals(RequestState.CONFIRMED)) {
                    throw new NotPossibleException("Request already confirmed");
                } else {
                    throw new BadRequestException("Request " + requestDto.id() + " is not pending");
                }
            }

            if (request.getStatus().equals("CONFIRMED")) {
                processConfirmedStatus(
                        event,
                        requestDto.toBuilder()
                                .status(RequestState.CONFIRMED)
                                .build(),
                        confirmedReqs
                );
            } else {
                processRejectedStatus(
                        requestDto.toBuilder()
                                .status(RequestState.REJECTED)
                                .build(),
                        canceledReqs
                );
            }
        }

        eventRepository.save(event);
        return new EventRequestStatusUpdateResult(confirmedReqs, canceledReqs);
    }

    private void processConfirmedStatus(
            Event event, RequestDto requestDto,
            List<RequestDto> confirmedRequests
    ) {
        if (event.getConfirmedRequests() >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            RequestDto canceledRequest = requestClient.update(requestDto);
            requestMap.put(requestDto.id(), requestMapper.toRequest(canceledRequest));
            confirmedRequests.add(canceledRequest);
            throw new NotPossibleException("The participant limit is reached");
        }

        RequestDto confirmedRequest = requestClient.update(requestDto);
        requestMap.put(requestDto.id(), requestMapper.toRequest(confirmedRequest));
        event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        confirmedRequests.add(confirmedRequest);
    }

    private void processRejectedStatus(
            RequestDto requestDto,
            List<RequestDto> canceledReqs
    ) {
        RequestDto rejectedRequest = requestClient.update(requestDto);
        requestMap.put(requestDto.id(), requestMapper.toRequest(rejectedRequest));
        canceledReqs.add(rejectedRequest);
    }

    @Override
    public List<EventFullResponseDto> adminGetEvents(AdminGetEventRequestDto requestParams) {
        List<Event> events = eventRepository.findEventsByAdmin(
                requestParams.users(),
                requestParams.states(),
                requestParams.categories(),
                requestParams.rangeStart(),
                requestParams.rangeEnd(),
                PageRequest.of(requestParams.from() / requestParams.size(),
                requestParams.size())
        );

        return listConverter.convertList(events.stream()
                .map(getEventEventFunction())
                .toList()
        );
    }

    @Override
    public EventFullResponseDto adminChangeEvent(Long eventId, UpdateEventUserRequest eventDto) {
        Event event = getEvent(eventId);
        checkEventForUpdate(event, eventDto.stateAction());
        Event updatedEvent = eventRepository.save(prepareEventForUpdate(event, eventDto));
        return converter.convert(updatedEvent, EventFullResponseDto.class);
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND_MESSAGE));
    }

    private void checkEventForUpdate(Event event, StateAction action) {
        checkEventDate(event.getEventDate());
        if (action == null) return;
        if (action.equals(StateAction.PUBLISH_EVENT)
                && !event.getState().equals(EventState.PENDING))
            throw new ParameterConflictException("Events", "Опубликовать событие можно в статусе PENDING, а статус = "
                                                           + event.getState());
        if (action.equals(REJECT_EVENT)
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
        if (eventDto.confirmedRequests() != null) {
            foundEvent.setConfirmedRequests(eventDto.confirmedRequests());
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
}
