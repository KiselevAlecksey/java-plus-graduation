package ru.practicum.user.userAdmin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.repository.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserAdminService {
   private final UserRepository userRepository;
   private final UserMapper userMapper;

    public UserDto createUser(UserDto userDto) {
        checkUserEmailValidation(userDto);
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(userDto)));
    }

    private void checkUserEmailValidation(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new DataIntegrityViolationException("Пользователь с таким email уже существует");
        }
    }

    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Page<User> users;
        Pageable pageable = PageRequest.of(from, size);
        if (ids == null) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findUsers(ids, pageable);
        }
        return users.stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    public void deleteUser(Long userId) {
        userRepository.delete(getUser(userId));
    }

    public UserDto getUserDto(Long userId) {
        return userMapper.toUserDto(getUser(userId));
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователя не существует"));
    }
}
