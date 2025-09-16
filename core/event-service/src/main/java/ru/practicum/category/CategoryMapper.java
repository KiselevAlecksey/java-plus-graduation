package ru.practicum.category;

import org.mapstruct.Mapper;
import ru.practicum.dto.CategoryCreateDto;
import ru.practicum.dto.CategoryResponseDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryCreateDto createDto);

    CategoryResponseDto toCategoryDto(Category category);

}
