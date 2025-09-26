package ru.practicum.category;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.CategoryResponseDto;

import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @RestLogging
    @GetMapping("/{categoryId}")
    public CategoryResponseDto getById(@PathVariable long categoryId) {
        return categoryService.getById(categoryId);
    }


    @RestLogging
    @GetMapping
    public List<CategoryResponseDto> getFromSize(
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return categoryService.getFromSize(from, size);
    }
}
