package ru.practicum.dto;

import lombok.Data;
import ru.practicum.dto.UserShortDto;

import java.util.Set;

@Data
public class SubscriptionDto {
    private Set<UserShortDto> subscribers;
    private Set<UserShortDto> blackList;
}
