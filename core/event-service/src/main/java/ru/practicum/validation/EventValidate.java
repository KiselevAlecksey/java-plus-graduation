package ru.practicum.validation;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

import ru.practicum.dto.EventRequest;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.exception.BadRequestException;

import java.time.LocalDateTime;

@Slf4j
public class EventValidate {

    public static final int MIN_ANNOTATION = 20;
    public static final int MIN_DESCRIPTION = 20;
    public static final int MAX_DESCRIPTION = 7000;
    public static final int MAX_ANNOTATION = 2000;
    public static final int MIN_TITLE = 3;
    public static final int MAX_TITLE = 120;

    public static void eventDateValidate(EventRequest dto) {
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
            checkLength(dto.description(), MIN_DESCRIPTION, MAX_DESCRIPTION, "Описание");
        }
        if (dto.annotation() != null) {
            checkLength(dto.annotation(), MIN_ANNOTATION, MAX_ANNOTATION, "Краткое описание");
        }
        if (dto.title() != null) {
            checkLength(dto.title(), MIN_TITLE, MAX_TITLE, "Заголовок");
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
