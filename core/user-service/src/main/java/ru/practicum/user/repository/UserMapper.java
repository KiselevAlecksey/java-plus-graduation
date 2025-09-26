package ru.practicum.user.repository;

import org.mapstruct.Mapper;
<<<<<<<< HEAD:core/user-service/src/main/java/ru/practicum/user/repository/UserMapper.java

import org.springframework.stereotype.Component;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
========
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
>>>>>>>> 4608889 (add create microservices):core/main-service/src/main/java/ru/practicum/user/repository/UserMapper.java
import ru.practicum.user.model.User;

@Mapper(componentModel = "spring")
@Component("userRepositoryMapper")
public interface UserMapper {

    UserDto toUserDto(User user);

    User toUser(UserDto userDto);

    UserShortDto toUserShortDto(User user);
}
