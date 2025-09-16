package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Builder(toBuilder = true)
public record AdminGetEventRequestDto(
        List<Long> users,
        List<EventState> states,
        List<Long> categories,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeStart,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeEnd,
        Integer from,
        Integer size
) {
    @JsonCreator
    public AdminGetEventRequestDto(
            @JsonProperty("users") List<Long> users,
            @JsonProperty("states") List<EventState> states,
            @JsonProperty("categories") List<Long> categories,
            @JsonProperty("rangeStart") String rangeStart,
            @JsonProperty("rangeEnd") String rangeEnd,
            @JsonProperty("from") Integer from,
            @JsonProperty("size") Integer size) {

        this(users, states, categories,
                parseDateTime(rangeStart),
                parseDateTime(rangeEnd),
                from, size);
    }

    private static LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTime, formatter);
    }
}
