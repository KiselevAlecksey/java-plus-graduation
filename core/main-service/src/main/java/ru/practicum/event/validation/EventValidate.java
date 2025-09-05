package ru.practicum.event.validation;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.exception.BadRequestException;

import java.time.LocalDateTime;

@Slf4j
public class EventValidate {

    public static void eventDateValidate(NewEventDto dto) {
        LocalDateTime dateTime = dto.eventDate();
        if (dateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            String messageError = "Событие должно начинаться не раньше чем через 2 часа.";
            log.error(messageError);
            throw new ValidationException(messageError);
        }
    }

    public static void updateEventDateValidate(UpdateEventUserRequest dto) {
        if (dto.eventDate() != null) {
            if (dto.eventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                String messageError = "Событие должно начинаться не раньше чем через 2 часа.";
                log.error(messageError);
                throw new BadRequestException(messageError);
            }
        }
    }

    public static void textLengthValidate(UpdateEventUserRequest dto) {
        if (dto.description() != null) {
            checkLength(dto.description(), 20, 7000, "Описание");
        }
        if (dto.annotation() != null) {
            checkLength(dto.annotation(), 20, 2000, "Краткое описание");
        }
        if (dto.title() != null) {
            checkLength(dto.title(), 3, 120, "Заголовок");
        }
    }

    private static void checkLength(String text, int min, int max, String name) {
        if (text.length() < min || text.length() > max) {
            String messageError = String.format("%s не может быть меньше %d или больше %d символов", name, min, max);
            log.error(messageError);
            throw new ValidationException(messageError);
        }
    }
}
