package ru.practicum.compilation.dto;

import lombok.Builder;
import ru.practicum.dto.EventShortResponseDto;

import java.util.Collection;


@Builder
public record CompilationDto(
       Collection<EventShortResponseDto> events,
        Long id,
        boolean pinned,
        String title) {
}
