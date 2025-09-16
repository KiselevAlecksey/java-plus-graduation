package ru.practicum.subscription.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aspect.RestLogging;
import ru.practicum.dto.EventShortResponseDto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.dto.SubscriptionDto;
import ru.practicum.subscription.model.BlackList;
import ru.practicum.subscription.model.Subscriber;
import ru.practicum.subscription.service.SubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @RestLogging
    @PostMapping("/subscriptions/{subscriberId}")
    public void addSubscribe(@PathVariable("userId") @Positive @NotNull long userId,
                             @PathVariable("subscriberId") @Positive @NotNull long subscriberId) {
        if (userId == subscriberId) {
            throw new ConditionsNotMetException("Пользователь не может подписаться сам на себя");
        }
        Subscriber subscriber = new Subscriber();
        subscriber.setUserId(userId);
        subscriber.setSubscriber(subscriberId);
        subscriptionService.addSubscriber(subscriber);
    }

    @RestLogging
    @PostMapping("black-list/{blackListId}")
    public void addBlackList(@PathVariable("userId") @Positive @NotNull long userId,
                             @PathVariable("blackListId") @Positive @NotNull long blackListId) {
        if (userId == blackListId) {
            throw new ConditionsNotMetException("Пользователь не может добавить в черный список сам на себя");
        }

        BlackList blackList = new BlackList();
        blackList.setUserId(userId);
        blackList.setBlackList(blackListId);
        subscriptionService.addBlacklist(blackList);

    }

    @RestLogging
    @DeleteMapping("/subscriptions/{subscriberId}")
    public void removeSubscriber(@PathVariable("userId") @Positive @NotNull long userId,
                                 @PathVariable("subscriberId") @Positive @NotNull long subscriberId) {
       subscriptionService.removeSubscriber(userId, subscriberId);
    }

    @RestLogging
    @DeleteMapping("/black-list/{blackListId}")
    public void removeBlackList(@PathVariable("userId") @Positive @NotNull long userId,
                                @PathVariable("blackListId") @Positive @NotNull long blackListId) {
        subscriptionService.removeFromBlackList(userId, blackListId);
    }

    @RestLogging
    @GetMapping("/subscriptions")
    public SubscriptionDto getListSubscriptions(@PathVariable("userId") @Positive @NotNull long userId) {
        SubscriptionDto subscriptionDto = subscriptionService.getSubscribers(userId);
        return subscriptionDto;
    }

    @RestLogging
    @GetMapping("/black-list")
    public SubscriptionDto getBlackListSubscriptions(@PathVariable("userId") @Positive @NotNull long userId) {
        SubscriptionDto subscriptionDto = subscriptionService.getBlacklists(userId);
        return subscriptionDto;
    }

    @RestLogging
    @GetMapping("/subscriptions/events")
    public List<EventShortResponseDto> getEventsSubscriptions(@PathVariable("userId") @Positive @NotNull long userId) {
        List<EventShortResponseDto> eventShortResponseDtos = subscriptionService.getEvents(userId);
        return eventShortResponseDtos;
    }
}
