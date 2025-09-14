package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.model.User;

@Mapper(componentModel = "spring")
public interface UserMapperInteraction {

    UserDto toUserDto(User user);

    User toUser(UserDto userDto);

    @Mapping(source = "userId", target = "id")
    UserShortDto toUserShortDto(Long userId);

    @Mapping(source = "id", target = "id")
    UserShortDto toUserShortDto(UserDto user);
}
