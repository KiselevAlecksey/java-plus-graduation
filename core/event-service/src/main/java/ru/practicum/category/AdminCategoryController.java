package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.CategoryCreateDto;
import ru.practicum.dto.CategoryResponseDto;
import ru.practicum.dto.CategoryUpdateDto;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @RestLogging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto create(@RequestBody @Validated CategoryCreateDto createDto) {
        return categoryService.create(createDto);
    }

    @RestLogging
    @PatchMapping("/{categoryId}")
    public CategoryResponseDto update(
            @RequestBody @Validated CategoryUpdateDto updateDto,
                                      @PathVariable long categoryId
    ) {
        CategoryUpdateDto updateDtoWithCatIdParam = new CategoryUpdateDto(categoryId, updateDto.name());
        CategoryResponseDto updated = categoryService.update(updateDtoWithCatIdParam);
        return updated;
    }

    @RestLogging
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long categoryId) {
        categoryService.remove(categoryId);
    }
}
