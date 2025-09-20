package ru.practicum.dto.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.dto.EventRequest;
import ru.practicum.model.Event;
import ru.practicum.model.Location;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class NewEventDtoToEventInteraction implements Converter<EventRequest, Event> {

    @Override
    public Event convert(EventRequest source) {
        Event event = new Event();
        event.setAnnotation(source.annotation());
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(source.description());
        event.setEventDate(source.eventDate());
        event.setPaid(source.paid());
        event.setParticipantLimit(source.participantLimit());
        event.setRequestModeration(source.requestModeration());
        event.setTitle(source.title());
        event.setLocation(new Location(source.location().getLat(), source.location().getLon()));
        return event;
    }
}
