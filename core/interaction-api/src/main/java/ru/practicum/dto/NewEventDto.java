package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import ru.practicum.model.Location;
import ru.practicum.validation.ValidEventDate;

import java.time.LocalDateTime;

public record NewEventDto(

        @NotBlank
        @Size(min = 3, max = 120)
        String title,

        @NotNull
        Long category,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,

        @NotNull
        Location location,

        @NotBlank
        @Size(min = 20, max = 2000)
        String annotation,

        @NotBlank
        @Size(min = 20, max = 7000)
        String description,


        @PositiveOrZero
        Long participantLimit,
        Boolean paid,
        Boolean requestModeration) {

        public NewEventDto(String title, Long category, LocalDateTime eventDate,
                           Location location, String annotation, String description,
                           Long participantLimit, Boolean paid, Boolean requestModeration) {
                this.title = title;
                this.category = category;
                this.eventDate = eventDate;
                this.location = location;
                this.annotation = annotation;
                this.description = description;
                this.participantLimit = participantLimit != null ? participantLimit : 0;
                this.paid = paid != null ? paid : false;
                this.requestModeration = requestModeration != null ? requestModeration : true;
        }

}
