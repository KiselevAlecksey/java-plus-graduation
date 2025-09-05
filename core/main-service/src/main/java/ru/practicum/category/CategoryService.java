package ru.practicum.category;

import ru.practicum.category.dto.CategoryCreateDto;
import ru.practicum.category.dto.CategoryResponseDto;
import ru.practicum.category.dto.CategoryUpdateDto;

import java.util.List;

public interface CategoryService {

    CategoryResponseDto create(CategoryCreateDto createDto);

    CategoryResponseDto update(CategoryUpdateDto updateDto);

    void remove(long categoryId);

    CategoryResponseDto getById(long categoryId);

    List<CategoryResponseDto> getFromSize(int from, int size);
}
