package ru.practicum.user.repository;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.user.model.User;

@Mapper(componentModel = "spring")
@Component("userRepositoryMapper")
public interface UserMapper {

    UserDto toUserDto(User user);

    User toUser(UserDto userDto);

    UserShortDto toUserShortDto(User user);
}
