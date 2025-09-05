package ru.practicum.user.userAdmin;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserAdminController {

   private final UserAdminService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Попытка создания нового пользователя {}", userDto);
        UserDto newUser = userService.createUser(userDto);
        log.info("Пользователь создан {}", newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUser(@RequestParam(required = false) List<Long> ids,
                                                 @RequestParam(required = false, defaultValue = "0")@PositiveOrZero Integer from,
                                                 @RequestParam(required = false, defaultValue = "10")@Positive Integer size) {
        log.info("Запрос списка пользователей с ID: {}", ids);
        return ResponseEntity.ok().body(userService.getUsers(ids, from, size));

    }

    @DeleteMapping("{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);
        userService.deleteUser(userId);
    }

}
