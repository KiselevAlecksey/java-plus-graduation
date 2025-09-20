package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.*;
import ru.practicum.model.Event;

@Mapper(componentModel = "spring", uses = CategoryMapperInteraction.class)
public interface EventMapperInteraction {

    @Mapping(source = "category.id", target = "category")
    NewEventDto toNewEventDto(Event event);

    @Mapping(source = "initiator.id", target = "initiator")
    Event toEventFromEventFullResponseDto(EventFullResponseDto dto);

    @Mapping(source = "category", target = "category")
    @Mapping(target = "initiator", ignore = true)
    EventFullResponseDto toEventFullResponseDtoFromEvent(Event event);

    EventShortResponseDto toEventShortResponseDtoFromEventFullResponseDto(EventFullResponseDto dto);

    @Mapping(source = "category.id", target = "category")
    @Mapping(target = "stateAction", ignore = true)
    UpdateEventUserRequest toUpdateEventUserRequestFromEvent(Event dto);
}
