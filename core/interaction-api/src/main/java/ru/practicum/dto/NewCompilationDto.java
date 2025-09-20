package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

public record NewCompilationDto(
        Set<Long> events,
        Boolean pinned,
        @NotBlank
        @Size(min = 1, max = 50)
        String title) {

        public NewCompilationDto {
                events = events != null ? events : new HashSet<>();
                pinned = pinned != null ? pinned : false;
        }
}
