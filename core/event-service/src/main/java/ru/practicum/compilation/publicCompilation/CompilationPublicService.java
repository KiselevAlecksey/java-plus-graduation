package ru.practicum.compilation.publicCompilation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CompilationPublicService {

   private final CompilationRepository compilationRepository;
    @Qualifier("mvcConversionService")
    private final ConversionService converter;

    public CompilationDto getCompilationById(Long compId) {
        return converter.convert(compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка c ID: " + compId + " не найдена или недоступна")),
                CompilationDto.class);
    }

    public List<CompilationDto> getCompilationsByParam(Boolean pinned, Integer from, Integer size) {
        Page<Compilation> compilations;
        Pageable pageable = PageRequest.of(from, size);

        compilations = compilationRepository.findCompilations(pinned,pageable);

        return compilations.stream()
                .map(comp -> converter.convert(comp, CompilationDto.class))
                .toList();
    }
}

