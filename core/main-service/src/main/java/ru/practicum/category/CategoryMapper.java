package ru.practicum.category;

import org.mapstruct.Mapper;
import ru.practicum.category.dto.CategoryCreateDto;
import ru.practicum.category.dto.CategoryResponseDto;
import ru.practicum.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryCreateDto createDto);

    CategoryResponseDto toCategoryDto(Category category);

}
