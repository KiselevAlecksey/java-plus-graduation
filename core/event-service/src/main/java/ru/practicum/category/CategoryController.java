package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryResponseDto;


import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{categoryId}")
    public CategoryResponseDto getById(@PathVariable long categoryId) {
        log.info("==> Get category {} start", categoryId);
        CategoryResponseDto created = categoryService.getById(categoryId);
        log.info("<== Get category {} end", created);
        return created;
    }



    @GetMapping
    public List<CategoryResponseDto> getFromSize(@RequestParam(defaultValue = "0") int from,
                                           @RequestParam(defaultValue = "10") int size) {
        log.info("==> category get from {} and size {} start", from, size);
        List<CategoryResponseDto> list = categoryService.getFromSize(from, size);
        log.info("<== category get complete");
        return list;
    }
}
