package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.HitDto;
import ru.practicum.model.Hit;

@Mapper(componentModel = "spring")
public interface HitMapper {

    Hit toHit(HitDto hitDto);

    HitDto toHitDto(Hit hit);

}
