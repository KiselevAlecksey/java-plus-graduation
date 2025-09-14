package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.model.ErrorResponse;

import java.util.Map;


@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException e) {
        log.info("Получен статус 404 Not found {}", e.getMessage(), e);

        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleParameterConflict(final ParameterConflictException e) {
        log.info("Received status 409 Conflict {}", e.getMessage(), e);

        return new ErrorResponse("Некорректное значение параметра " + e.getParameter() + ": " + e.getReason());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleParameterNotValid(final ParameterNotValidException e) {
        log.trace("Получен статус 400 Bad request {}", e.getMessage(), e);

        return new ErrorResponse("Некорректное значение параметра " + e.getParameter() + ": " + e.getReason());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConditionsNotMetException(final ConditionsNotMetException e) {
        log.trace("Получен статус 400 Bad request {}", e.getMessage(), e);

        return new ErrorResponse("Условия не соблюдены: " + e.getMessage());
    }

    /*@ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleParameterNotValid(final MethodArgumentNotValidException e) {
        log.info("Получен статус 400 Bad request {}", e.getMessage(), e);

        String message = null;

        FieldError fieldError = e.getBindingResult().getFieldError();

        if (fieldError != null) {
            message = fieldError.getDefaultMessage();
        }

        return new ErrorResponse("Некорректное значение параметра "
                + e.getParameter().getParameterName() + ": ", message);
    }*/

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(final MethodArgumentNotValidException e) {
        log.info("Получен статус 400 Bad request {}", e.getMessage(), e);

        return new ErrorResponse("Некорректное значение параметра: " + e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConstraintViolationException(ConstraintViolationException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequestException(BadRequestException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleNotPossible(NotPossibleException e) {
        log.info("Received status 409 Conflict {}", e.getMessage(), e);

        return new ErrorResponse("Некорректное значение параметра ");
    }
}
