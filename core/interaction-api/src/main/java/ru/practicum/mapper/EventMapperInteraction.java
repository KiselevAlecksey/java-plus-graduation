package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.EventFullResponseDto;
import ru.practicum.dto.EventShortResponseDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapperInteraction {

    @Mapping(source = "category.id", target = "category")
    NewEventDto toNewEventDto(Event event);

    @Mapping(source = "initiator.id", target = "initiator")
    Event toEventFromEventFullResponseDto(EventFullResponseDto dto);

    EventShortResponseDto toEventShortResponseDtoFromEventFullResponseDto(EventFullResponseDto dto);
}
