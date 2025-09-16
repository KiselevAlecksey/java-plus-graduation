package ru.practicum;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;

@RestController
public class FallbackController {

    @GetMapping("/fallback/internal/admin/users")
    public ResponseEntity<Collection<UserDto>> getUsersFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Collections.emptyList());
    }

    @GetMapping("/fallback/internal/admin/users/{userId}")
    public ResponseEntity<UserDto> getUserFallback(@PathVariable Long userId) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @GetMapping("/fallback/internal/users/requests/{requestId}")
    public ResponseEntity<RequestDto> getRequestFallback(@PathVariable long requestId) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @GetMapping("/fallback/internal/users/requests")
    public ResponseEntity<Collection<RequestDto>> getRequestsFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Collections.emptyList());
    }

    @PostMapping("/fallback/internal/users/requests")
    public ResponseEntity<RequestDto> saveRequestFallback(@RequestBody RequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @PostMapping("/fallback/internal/users/requests/update")
    public ResponseEntity<RequestDto> updateRequestFallback(@RequestBody RequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @GetMapping("/fallback/internal/users/{userId}/events/{id}")
    public ResponseEntity<EventFullResponseDto> getEventByIdFallback(
            @PathVariable Long userId, @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @PostMapping("/fallback/internal/users/{userId}/events")
    public ResponseEntity<EventFullResponseDto> createEventFallback(
            @PathVariable Long userId, @RequestBody EventFullResponseDto event) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @PatchMapping("/fallback/internal/users/{userId}/events")
    public ResponseEntity<EventFullResponseDto> updateEventFallback(
            @PathVariable Long userId, @RequestBody UpdateEventUserRequest event) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @GetMapping("/fallback/internal/users/{userId}/events")
    public ResponseEntity<EventFullResponseDto> getEventByInitiatorIdFallback(
            @PathVariable Long userId) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @RequestMapping("/fallback/**")
    public ResponseEntity<ErrorDto> generalFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorDto.builder()
                        .message("Service unavailable")
                        .userMessage("Please try again later")
                        .httpStatus("SERVICE_UNAVAILABLE")
                        .build());
    }
}