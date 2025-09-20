package ru.practicum.compilation.adminCompilation;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("admin/compilations")
public class CompilationAdminController {
    private final CompilationAdminService compilationAdminService;

    @RestLogging
    @PostMapping
    public ResponseEntity<CompilationDto> createCompilation(
            @RequestBody @Valid NewCompilationDto newCompilationDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compilationAdminService.createCompilation(newCompilationDto));
    }

    @RestLogging
    @DeleteMapping("{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        compilationAdminService.deleteCompilation(compId);
    }

    @RestLogging
    @PatchMapping("{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(
            @RequestBody @Valid UpdateCompilationRequest upComp,
            @PathVariable Long compId
    ) {
       return ResponseEntity.ok().body(compilationAdminService.updateCompilation(upComp, compId));
    }

}
