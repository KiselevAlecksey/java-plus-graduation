package ru.practicum.compilation.publicCompilation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/compilations")
public class CompilationPublicController {

    private final CompilationPublicService compilationPublicService;

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable @Positive Long compId) {
        log.info("Запрос подборки событий с ID: {}", compId);
       return compilationPublicService.getCompilationById(compId);
    }

    @GetMapping
    public List<CompilationDto> getCompilationsByParam(@RequestParam(required = false) Boolean pinned,
                                     @RequestParam(required = false, defaultValue = "0")@PositiveOrZero Integer from,
                                     @RequestParam(required = false, defaultValue = "10")@Positive Integer size) {
        log.info("Запрос подборок событий с параметрами");
        return compilationPublicService.getCompilationsByParam(pinned, from, size);
    }

}
