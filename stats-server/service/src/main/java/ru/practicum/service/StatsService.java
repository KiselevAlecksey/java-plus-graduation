package ru.practicum.service;

import ru.practicum.HitDto;
import ru.practicum.StatDto;
import ru.practicum.StatRequestDto;

import java.util.List;

public interface StatsService {

    HitDto hit(HitDto hitDto);

    List<StatDto> getStats(StatRequestDto statDto);

    Long getEventViews(String uri);
}
