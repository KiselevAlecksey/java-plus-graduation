package ru.practicum.compilation.dto.converter;


import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.dto.converter.EventToEventShortResponseDtoConverterInteraction;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;


@RequiredArgsConstructor
@Component
public class CompilationToCompilationDtoConverter implements Converter<Compilation, CompilationDto> {
    private final EventToEventShortResponseDtoConverterInteraction converter;

    @Override
    public CompilationDto convert(Compilation source) {
        return CompilationDto.builder()
                .id(source.getId())
                .title(source.getTitle())
                .pinned(source.getPinned())
                .events(source.getEvents().stream().map(event -> converter.convert(
                        Event.builder()
                                .id(event.getId())
                                .initiator(event.getInitiator())
                                .title(event.getTitle())
                                .category(new Category(event.getCategory().getId(), event.getCategory().getName()))
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
                                .build())).toList())
                .build();
    }
}
