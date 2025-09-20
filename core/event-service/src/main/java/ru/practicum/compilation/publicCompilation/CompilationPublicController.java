package ru.practicum.compilation.publicCompilation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.CompilationDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/compilations")
public class CompilationPublicController {

    private final CompilationPublicService compilationPublicService;

    @RestLogging
    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable @Positive Long compId) {
       return compilationPublicService.getCompilationById(compId);
    }

    @RestLogging
    @GetMapping
    public List<CompilationDto> getCompilationsByParam(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(required = false, defaultValue = "0")@PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10")@Positive Integer size
    ) {
        return compilationPublicService.getCompilationsByParam(pinned, from, size);
    }

}
