package ru.practicum.fallback;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class ErrorDto {
    private Throwable cause;

    private StackTraceElement[] stackTrace;

    private String httpStatus;

    private String userMessage;

    private String message;

    private Throwable[] suppressed;

    private String localizedMessage;
}

