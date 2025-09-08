package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatDto;
import ru.practicum.StatRequestDto;
import ru.practicum.mapper.HitMapper;
import ru.practicum.repository.StatsRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class StatsServiceImpl implements StatsService {

   private final StatsRepository statsRepository;
   private final HitMapper hitMapper;

    @Override
    public HitDto hit(HitDto hitDto) {
        return hitMapper.toHitDto(statsRepository.save(hitMapper.toHit(hitDto)));
    }

    @Override
    public List<StatDto> getStats(StatRequestDto statDto) {
        if (statDto.getUnique()) {
            return statsRepository.getWithUniqueIpCount(statDto.getStart(), statDto.getEnd(), statDto.getUri());
        } else {
            return statsRepository.getWithIpCount(statDto.getStart(), statDto.getEnd(), statDto.getUri());
        }
    }

    @Override
    public Long getEventViews(String uri) {
        return statsRepository.viewsCount(uri);
    }
}