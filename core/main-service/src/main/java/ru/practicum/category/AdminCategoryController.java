package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryCreateDto;
import ru.practicum.category.dto.CategoryResponseDto;
import ru.practicum.category.dto.CategoryUpdateDto;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto create(@RequestBody @Validated CategoryCreateDto createDto) {
        log.info("==> Create category {} start", createDto);
        CategoryResponseDto created = categoryService.create(createDto);
        log.info("<== Created category {} end", created);
        return created;
    }

    @PatchMapping("/{categoryId}")
    public CategoryResponseDto update(@RequestBody @Validated CategoryUpdateDto updateDto,
                                      @PathVariable long categoryId) {
        log.info("==> Update category {} start", updateDto);
        CategoryUpdateDto updateDtoWithCatIdParam = new CategoryUpdateDto(categoryId, updateDto.name());
        CategoryResponseDto updated = categoryService.update(updateDtoWithCatIdParam);
        log.info("<== Updated category {} end", updated);
        return updated;
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long categoryId) {
        log.info("==> Remove category {} start", categoryId);
        categoryService.remove(categoryId);
        log.info("<== Remove category {} end", categoryId);
    }
}
