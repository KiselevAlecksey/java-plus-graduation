package ru.practicum.compilation.adminCompilation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("admin/compilations")
public class CompilationAdminController {

    private final CompilationAdminService compilationAdminService;

    @PostMapping
    public ResponseEntity<CompilationDto> createCompilation(@RequestBody @Validated NewCompilationDto newCompilationDto) {
        log.info("Создание подборки событий {}", newCompilationDto);
        CompilationDto newComp = compilationAdminService.createCompilation(newCompilationDto);
        log.info("Подборка событий создана с ID: {}", newComp.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(newComp);
    }

    @DeleteMapping("{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Попытка удаления подборки событий с ID: {}", compId);
        compilationAdminService.deleteCompilation(compId);
        log.info("Подборка с ID: {} удалена", compId);
    }

    @PatchMapping("{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(@RequestBody @Validated UpdateCompilationRequest upComp,
                                                            @PathVariable Long compId) {
        log.info("Попытка обновления подборки событий с ID: {}", compId);
       CompilationDto updateComp = compilationAdminService.updateCompilation(upComp, compId);
       log.info("Подборка с ID: {} обновлена", compId);
       return ResponseEntity.status(HttpStatus.OK).body(updateComp);
    }

}
