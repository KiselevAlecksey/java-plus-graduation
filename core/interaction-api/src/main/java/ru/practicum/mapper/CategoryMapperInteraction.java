package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.model.Category;
import ru.practicum.dto.CategoryCreateDto;
import ru.practicum.dto.CategoryResponseDto;

@Mapper(componentModel = "spring")
public interface CategoryMapperInteraction {

    Category toCategory(CategoryCreateDto createDto);

    CategoryResponseDto toCategoryDto(Category category);

}
