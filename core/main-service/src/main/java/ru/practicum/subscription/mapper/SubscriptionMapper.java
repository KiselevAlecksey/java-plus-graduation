package ru.practicum.subscription.mapper;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.subscription.dto.SubscriptionDto;
import ru.practicum.subscription.model.BlackList;
import ru.practicum.subscription.model.Subscriber;
import ru.practicum.user.repository.UserMapper;
import ru.practicum.user.userAdmin.UserAdminService;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {
    private final UserMapper userMapper;
    private final UserAdminService userService;

    public SubscriptionDto subscribertoSubscriptionDto(List<Subscriber> subscriber) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setSubscribers(subscriber.stream()
                .map(Subscriber::getSubscriber)
                .map(userService::getUser)
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toSet())
        );
        return dto;
    }

    public SubscriptionDto blackListSubscriptionDto(List<BlackList> blackList) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setBlackList(blackList.stream()
                .map(BlackList::getBlackList)
                .map(userService::getUser)
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toSet())
        );
        return dto;
    }
}
