package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpdateDto(

        long id,

        @NotBlank
        @Size(max = 50)
        String name) {

}
