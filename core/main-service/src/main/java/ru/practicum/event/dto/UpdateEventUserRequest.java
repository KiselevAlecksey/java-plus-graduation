package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ru.practicum.event.enums.StateAction;
import ru.practicum.event.model.Location;

import java.time.LocalDateTime;

public record UpdateEventUserRequest(


        @Size(min = 3, max = 120)
        String title,

        Long category,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,

        Location location,


        @Size(min = 20, max = 2000)
        String annotation,


        @Size(min = 20, max = 7000)
        String description,

        @PositiveOrZero
        Long participantLimit,

        Boolean paid,
        Boolean requestModeration,
        StateAction stateAction
) {

}
