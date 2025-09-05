package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.AdminGetEventRequestDto;
import ru.practicum.event.dto.EventFullResponseDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.event.validation.EventValidate;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullResponseDto> adminGetEvents(AdminGetEventRequestDto requestParams) {
        log.info("Получить события, согласно условиям -> {}", requestParams);
        return eventService.adminGetEvents(requestParams);
    }

    @PatchMapping("/{eventId}")
    public EventFullResponseDto adminChangeEvent(@PathVariable Long eventId,
                                     @RequestBody @Valid UpdateEventUserRequest eventDto) {
        log.info("Изменить событие eventId = {}, поля -> {}", eventId, eventDto);
        EventValidate.updateEventDateValidate(eventDto);
        EventValidate.textLengthValidate(eventDto);
        return eventService.adminChangeEvent(eventId, eventDto);
    }
}
