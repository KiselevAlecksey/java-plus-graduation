package ru.practicum.dto;

import lombok.Builder;
import java.util.Collection;

@Builder
public record CompilationDto(
        Collection<EventShortResponseDto> events,
        Long id,
        boolean pinned,
        String title) {
}
