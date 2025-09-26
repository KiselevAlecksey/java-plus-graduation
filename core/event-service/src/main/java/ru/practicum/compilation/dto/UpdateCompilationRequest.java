package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateCompilationRequest(
        Set<Long> events,
        boolean pinned,
        @Size(max = 50)
        String title
) {
}
