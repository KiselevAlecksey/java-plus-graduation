package ru.practicum.feign;

import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.UserDto;

import java.util.Collection;

public interface UserAdminControllerInternal {
    @GetMapping
    Collection<UserDto> getUsers();

    @GetMapping("/{userId}")
    UserDto getUser(@PathVariable Long userId);
}
