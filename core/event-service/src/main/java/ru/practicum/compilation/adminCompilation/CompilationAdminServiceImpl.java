package ru.practicum.compilation.adminCompilation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.ConversionFailedExceptionInteraction;
import ru.practicum.exception.NotFoundException;

@RequiredArgsConstructor
@Service
public class CompilationAdminServiceImpl implements CompilationAdminService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    @Qualifier("mvcConversionService")
    private final ConversionService converter;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation comp = converter.convert(newCompilationDto, Compilation.class);
        if (comp == null) {
            throw new ConversionFailedExceptionInteraction("Не удалось преобразовать DTO в сущность");
        }
        comp.setEvents(eventRepository.findAllById(newCompilationDto.events()));
        return converter.convert(compilationRepository.save(comp), CompilationDto.class);
    }

    @Override
    public void deleteCompilation(Long compId) {
        Compilation comp = getCompilation(compId);
        compilationRepository.delete(comp);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId) {
        Compilation newComp = getCompilation(compId);
        updateNewCompilation(updateCompilationRequest, newComp);
        return converter.convert(compilationRepository.save(newComp), CompilationDto.class);
    }

    private void updateNewCompilation(UpdateCompilationRequest updateCompilationRequest, Compilation newComp) {
        if (updateCompilationRequest.events() != null) {
            newComp.setEvents(eventRepository.findAllById(updateCompilationRequest.events()));
        }
        if (!ObjectUtils.isEmpty(updateCompilationRequest.pinned())) {
            newComp.setPinned(updateCompilationRequest.pinned());
        }
        if (updateCompilationRequest.title() != null) {
            newComp.setTitle(updateCompilationRequest.title());
        }
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> (new NotFoundException("Подборка с ID: " + compId + " отсутствует")));
    }
}
