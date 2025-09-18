package ru.practicum.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.CategoryCreateDto;
import ru.practicum.dto.CategoryResponseDto;
import ru.practicum.dto.CategoryUpdateDto;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @RestLogging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto create(@Valid @RequestBody CategoryCreateDto createDto) {
        return categoryService.create(createDto);
    }

    @RestLogging
    @PatchMapping("/{categoryId}")
    public CategoryResponseDto update(
            @Valid @RequestBody CategoryUpdateDto updateDto,
            @PathVariable long categoryId
    ) {
        return categoryService.update(new CategoryUpdateDto(categoryId, updateDto.name()));
    }

    @RestLogging
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long categoryId) {
        categoryService.remove(categoryId);
    }
}
