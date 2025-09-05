package ru.practicum.compilation.adminCompilation;

import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ParameterNotValidException;

import java.sql.SQLException;
import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class CompilationAdminService  {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    @Qualifier("mvcConversionService")
    private final ConversionService converter;

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {

        if (newCompilationDto == null || newCompilationDto.title() == null) {
            throw new ParameterNotValidException("Compilation", "Запрос составлен некорректно");
        }
        Compilation comp = converter.convert(newCompilationDto, Compilation.class);
        try {

        if (comp != null && newCompilationDto.events() != null) {
            comp.setEvents(eventRepository.findAllById(newCompilationDto.events()));
        } else if (comp != null) {
            comp.setEvents(new ArrayList<>());
        }
        if (comp == null) {
            throw new NoResultException();
        }
            return converter.convert(compilationRepository.save(comp), CompilationDto.class);
        } catch (PersistenceException e) {
            throw new ConstraintViolationException("Compilation", new SQLException(), "Нарушение целостности данных");
        }
    }

    public void deleteCompilation(Long compId) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка событий не найдена"));
        compilationRepository.delete(comp);
    }

    public CompilationDto updateCompilation(UpdateCompilationRequest upComp, Long compId) {
       Compilation newComp = compilationRepository.findById(compId)
                .orElseThrow(() -> (new NotFoundException("Подборка с ID: " + compId + " отсутствует")));

      if (upComp.events() != null) newComp.setEvents(eventRepository.findAllById(upComp.events()));
      if (!ObjectUtils.isEmpty(upComp.pinned())) newComp.setPinned(upComp.pinned());
      if (upComp.title() != null) newComp.setTitle(upComp.title());
      return converter.convert(compilationRepository.save(newComp), CompilationDto.class);
    }
}
