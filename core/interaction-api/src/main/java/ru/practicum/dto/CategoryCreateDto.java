package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder(toBuilder = true)
public record CategoryCreateDto(
        Long id,

        @NotBlank
                @Size(max = 50)
        String name
) {
}
