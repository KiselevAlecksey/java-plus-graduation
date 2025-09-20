package ru.practicum.fallback;

import lombok.Builder;

@Builder
public record UserShortDto(
        long id) {
}
