package ru.practicum.event.dto.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.category.CategoryMapper;
import ru.practicum.event.dto.EventFullResponseDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.user.repository.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class EventToEventFullResponseDtoConverter implements Converter<Event, EventFullResponseDto> {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    @Override
    public EventFullResponseDto convert(Event source) {
        return EventFullResponseDto.builder()
                .id(source.getId())
                .title(source.getTitle())
                .confirmedRequests(Long.valueOf(source.getConfirmedRequests()))
                .eventDate(source.getEventDate())
                .annotation(source.getAnnotation())
                .paid(source.getPaid())
                .createdOn(source.getCreatedOn())
                .description(source.getDescription())
                .state(source.getState())
                .participantLimit(source.getParticipantLimit())
                .location(new Location(source.getLocation().getLat(), source.getLocation().getLon()))
                .category(categoryMapper.toCategoryDto(source.getCategory()))
                .initiator(userMapper.toUserShortDto(source.getInitiator()))
                .requestModeration(source.getRequestModeration())
                .views((source.getViews() == null) ? 0L : source.getViews())
                .build();
    }

    public List<EventFullResponseDto> convertList(List<Event> sourceList) {
        return sourceList.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }
}
