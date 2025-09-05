package ru.practicum.event.dto.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class NewEventDtoToEvent implements Converter<NewEventDto, Event> {

    @Override
    public Event convert(NewEventDto source) {
        Event event = new Event();
        event.setAnnotation(source.annotation());
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(source.description());
        event.setEventDate(source.eventDate());
        event.setPaid(source.paid());
        event.setParticipantLimit(source.participantLimit());
        event.setRequestModeration(source.requestModeration());
        event.setTitle(source.title());
        event.setLocation(source.location());
        return event;
    }
}
