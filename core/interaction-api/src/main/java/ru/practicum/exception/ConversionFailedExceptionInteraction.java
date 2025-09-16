package ru.practicum.exception;

import org.springframework.core.convert.ConversionException;

public class ConversionFailedExceptionInteraction extends ConversionException {

    public ConversionFailedExceptionInteraction(String message) {
        super(message);
    }
}
