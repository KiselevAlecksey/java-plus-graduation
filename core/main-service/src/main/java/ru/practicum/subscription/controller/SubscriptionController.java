package ru.practicum.subscription.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventShortResponseDto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.subscription.dto.SubscriptionDto;
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

    @PostMapping("/subscriptions/{subscriberId}")
    public void addSubscribe(@PathVariable("userId") @Positive @NotNull long userId,
                             @PathVariable("subscriberId") @Positive @NotNull long subscriberId) {
        if (userId == subscriberId) {
            throw new ConditionsNotMetException("Пользователь не может подписаться сам на себя");
        }
        log.info("POST Запрос на добавление подписки на человека  >>");
        Subscriber subscriber = new Subscriber();
        subscriber.setUserId(userId);
        subscriber.setSubscriber(subscriberId);
        subscriptionService.addSubscriber(subscriber);
    }

    @PostMapping("black-list/{blackListId}")
    public void addBlackList(@PathVariable("userId") @Positive @NotNull long userId,
                             @PathVariable("blackListId") @Positive @NotNull long blackListId) {
        log.info("POST Запрос на добавление человека в черный список >>");
        if (userId == blackListId) {
            throw new ConditionsNotMetException("Пользователь не может добавить в черный список сам на себя");
        }

        BlackList blackList = new BlackList();
        blackList.setUserId(userId);
        blackList.setBlackList(blackListId);
        subscriptionService.addBlacklist(blackList);

    }

    @DeleteMapping("/subscriptions/{subscriberId}")
    public void removeSubscriber(@PathVariable("userId") @Positive @NotNull long userId,
                                 @PathVariable("subscriberId") @Positive @NotNull long subscriberId) {
        log.info("DELETE Запрос на удаление человека из списка подписок >>");
       subscriptionService.removeSubscriber(userId, subscriberId);
    }

    @DeleteMapping("/black-list/{blackListId}")
    public void removeBlackList(@PathVariable("userId") @Positive @NotNull long userId,
                                @PathVariable("blackListId") @Positive @NotNull long blackListId) {
        log.info("DELETE Запрос на удаление человека из черного списка >>");
        subscriptionService.removeFromBlackList(userId, blackListId);
    }

    @GetMapping("/subscriptions")
    public SubscriptionDto getListSubscriptions(@PathVariable("userId") @Positive @NotNull long userId) {
        log.info("GET Запрос на получение списка подписок человека с ID {}", userId);
        SubscriptionDto subscriptionDto = subscriptionService.getSubscribers(userId);
        log.info("GET Запрос {}", subscriptionDto);
        return subscriptionDto;
    }

    @GetMapping("/black-list")
    public SubscriptionDto getBlackListSubscriptions(@PathVariable("userId") @Positive @NotNull long userId) {
        log.info("GET Запрос на получение черного списка человека с ID {}", userId);
        SubscriptionDto subscriptionDto = subscriptionService.getBlacklists(userId);
        log.info("GET Запрос {}", subscriptionDto);
        return subscriptionDto;
    }

    @GetMapping("/subscriptions/events")
    public List<EventShortResponseDto> getEventsSubscriptions(@PathVariable("userId") @Positive @NotNull long userId) {
        log.info("GET Запрос на получение списка мероприятий пользователей на которых подписан человек с ID {} ", userId);
        List<EventShortResponseDto> eventShortResponseDtos = subscriptionService.getEvents(userId);
        log.info("GET Запрос на получение списка мероприятий выполнен {} ", eventShortResponseDtos);
        return eventShortResponseDtos;
    }
}
