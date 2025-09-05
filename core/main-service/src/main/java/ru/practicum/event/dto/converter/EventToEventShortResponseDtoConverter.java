package ru.practicum.event.dto.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.category.CategoryMapper;
import ru.practicum.event.dto.EventShortResponseDto;
import ru.practicum.event.model.Event;
import ru.practicum.user.dto.UserMapper;

@RequiredArgsConstructor
@Component
public class EventToEventShortResponseDtoConverter implements Converter<Event, EventShortResponseDto> {

   private final UserMapper userMapper;
   private final CategoryMapper categoryMapper;

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
