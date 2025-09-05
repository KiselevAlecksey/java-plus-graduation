package ru.practicum.user.dto;

import lombok.Builder;

@Builder
public record UserShortDto(
        long id,
        String name) {
}
