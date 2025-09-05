package ru.practicum.user.dto;

import org.mapstruct.Mapper;
import ru.practicum.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    User toUser(UserDto userDto);

    UserShortDto toUserShortDto(User user);
}
