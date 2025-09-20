package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.RequestDto;
import ru.practicum.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapperInteraction {

    @Mapping(source = "created", target = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "event", target = "event")
    @Mapping(source = "requester", target = "requester")
    RequestDto toRequestDto(Request request);

    @Mapping(source = "event", target = "event")
    @Mapping(source = "requester", target = "requester")
    Request toRequest(RequestDto dto);
}
