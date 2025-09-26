package ru.practicum.subscription.mapper;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.feign.UserFeignClient;
import ru.practicum.mapper.UserMapperInteraction;
import ru.practicum.dto.SubscriptionDto;
import ru.practicum.subscription.model.BlackList;
import ru.practicum.subscription.model.Subscriber;
<<<<<<<< HEAD:core/subscription-service/src/main/java/ru/practicum/subscription/mapper/SubscriptionMapper.java
========
import ru.practicum.user.repository.UserMapper;
import ru.practicum.user.userAdmin.UserAdminService;
>>>>>>>> 4608889 (add create microservices):core/main-service/src/main/java/ru/practicum/subscription/mapper/SubscriptionMapper.java

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {
    private final UserMapperInteraction userMapper;
    private final UserFeignClient userFeignClient;

    public SubscriptionDto subscribertoSubscriptionDto(List<Subscriber> subscriber) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setSubscribers(subscriber.stream()
                .map(Subscriber::getSubscriber)
                .map(userFeignClient::getUser)
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toSet())
        );
        return dto;
    }

    public SubscriptionDto blackListSubscriptionDto(List<BlackList> blackList) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setBlackList(blackList.stream()
                .map(BlackList::getBlackList)
                .map(userFeignClient::getUser)
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toSet())
        );
        return dto;
    }
}
