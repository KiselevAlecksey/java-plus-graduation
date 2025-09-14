package ru.practicum.dto.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.mapper.CategoryMapperInteraction;
import ru.practicum.dto.EventShortResponseDto;

import ru.practicum.mapper.UserMapperInteraction;
import ru.practicum.model.Event;

@RequiredArgsConstructor
@Component
public class EventToEventShortResponseDtoConverterInteraction implements Converter<Event, EventShortResponseDto> {

   private final UserMapperInteraction userMapper;
   private final CategoryMapperInteraction categoryMapper;

    @Override
    public EventShortResponseDto convert(Event source) {
        return EventShortResponseDto.builder()
                .id(source.getId())
                .paid(source.getPaid())
                .title(source.getTitle())
                .views(source.getViews())
                .eventDate(source.getEventDate())
                .annotation(source.getAnnotation())
                .initiator(userMapper.toUserShortDto(source.getInitiator()))
                .category(categoryMapper.toCategoryDto(source.getCategory()))
                .views(source.getViews())
                .build();
    }


}
