package ru.practicum.subscription.service;

import ru.practicum.dto.EventShortResponseDto;
import ru.practicum.model.User;
import ru.practicum.subscription.dto.SubscriptionDto;
import ru.practicum.subscription.model.BlackList;
import ru.practicum.subscription.model.Subscriber;

import java.util.List;

public interface SubscriptionService {
    void addSubscriber(Subscriber subscriber);

    void addBlacklist(BlackList blackList);

    void removeSubscriber(Long userId, Long subscriberId);

    SubscriptionDto getSubscribers(long userId);

    SubscriptionDto getBlacklists(long userId);

    List<EventShortResponseDto> getEvents(long userId);

    void removeFromBlackList(long userId, long blackListId);

    User getUser(Long userId);
}
