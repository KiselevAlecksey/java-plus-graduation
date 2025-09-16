package ru.practicum.user.userAdmin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.UserDto;
import ru.practicum.feign.UserAdminControllerInternal;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/users")
public class UserAdminControllerInternalImpl implements UserAdminControllerInternal {
    private final UserAdminService userService;

    @RestLogging
    @Override
    public Collection<UserDto> getUsers() {
        return userService.getUsers();
    }

    @RestLogging
    public UserDto getUser(Long userId) {
        return userService.getUserDto(userId);
    }
}
