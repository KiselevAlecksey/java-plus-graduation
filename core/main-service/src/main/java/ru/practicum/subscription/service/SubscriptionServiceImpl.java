package ru.practicum.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.dto.EventShortResponseDto;
import ru.practicum.event.dto.converter.EventToEventShortResponseDtoConverter;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.subscription.dto.SubscriptionDto;
import ru.practicum.subscription.mapper.SubscriptionMapper;
import ru.practicum.subscription.model.BlackList;
import ru.practicum.subscription.model.Subscriber;
import ru.practicum.subscription.repository.BlackListRepository;
import ru.practicum.subscription.repository.SubscriberRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.userAdmin.UserAdminService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriberRepository subscriberRepository;
    private final BlackListRepository blackListRepository;
    private final UserAdminService userService;
    private final EventService eventService;
    private final EventToEventShortResponseDtoConverter listConverter;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    public void addSubscriber(Subscriber subscriber) {
        log.debug("Проверка пользователя на существование в БД {}", subscriber.getUserId());
        User userSibscriber = getUser(subscriber.getUserId(), subscriber.getSubscriber());
        checkUserBD(subscriber.getUserId(), subscriber.getSubscriber());
        log.info("POST Запрос Сохранение пользователя в подписчиках {} {}", userSibscriber.getName(), userSibscriber.getEmail());
        subscriberRepository.save(subscriber);
    }

    @Override
    public void addBlacklist(BlackList blackList) {
        log.debug("Проверка пользователей на существование в БД {}", blackList.getUserId());
        User blockUser = getUser(blackList.getUserId(), blackList.getBlackList());
        checkUserBD(blackList.getUserId(), blackList.getBlackList());
        log.info("POST Запрос Сохранение пользователя в черный список {} {}", blockUser.getName(), blockUser.getEmail());
        blackListRepository.save(blackList);
    }

    @Override
    public void removeSubscriber(Long userId, Long subscriberId) {
        log.debug("Проверка пользователя на существование в БД {}", userId);
        getUser(userId, subscriberId);
        Optional<Subscriber> subscribed = subscriberRepository.findByUserIdAndSubscriber(userId, subscriberId);
       if (subscribed.isPresent()) {
           subscriberRepository.delete(subscribed.orElseThrow(() -> new NotFoundException("Пользователя нет в подписчиках")));
            log.info("DELETE Запрос на удаление пользователя из подписок выполнено");
        }
    }


    @Override
    public void removeFromBlackList(long userId, long blackListId) {
        log.debug("Проверка пользователей на существование в БД {}", userId);
        getUser(userId,blackListId);
        Optional<BlackList> blackLists = blackListRepository.findByUserIdAndBlockUser(userId, blackListId);
        if (blackLists.isPresent()) {
            blackListRepository.delete(blackLists.orElseThrow(() -> new NotFoundException("Пользователя нет в черном листе")));
            log.info("DELETE Запрос на удаление пользователя из черного списка выполнено");
        }
    }

    @Override
    public SubscriptionDto getSubscribers(long userId) {
        log.debug("Получение списка ID пользователей на которых подписаны");
        List<Subscriber> subscriptions = subscriberRepository.findAllByUserId(userId);
        log.info("GET Запрос на получение списка подписок пользователя выполнен {}", subscriptions);
        return subscriptionMapper.subscribertoSubscriptionDto(subscriptions);
    }

    @Override
    public SubscriptionDto getBlacklists(long userId) {
        log.debug("Получение списка ID пользователей на которые в черном списке");
        List<BlackList> blackList = blackListRepository.findAllByUserId(userId);
        log.info("GET Запрос на получение списка черного списка пользователя выполнен {}", blackList);
        return subscriptionMapper.blackListSubscriptionDto(blackList);
    }

    @Override
    public List<EventShortResponseDto> getEvents(long userId) {
        return subscriberRepository.findAllByUserId(userId).stream()
                .map(Subscriber::getSubscriber)
                .map(eventService::getEventByInitiator)
                .filter(Objects::nonNull)
                .filter(event -> event.getState().equals(EventState.PENDING)
                                 || event.getState().equals(EventState.PUBLISHED))
                .map(listConverter::convert)
                .toList();
    }

    private User getUser(long userId, long subscriberId) {
        userService.getUser(userId);
        return userService.getUser(subscriberId);
    }

    private void checkUserBD(long userId, long subscriberId) {
        if (subscriberRepository
                .findByUserIdAndSubscriber(userId, subscriberId)
                .isPresent()) {
            throw new ConditionsNotMetException("Пользователь уже в списке подписчиков на данного человека");
        }
        if (blackListRepository
                .findByUserIdAndBlockUser(userId, subscriberId)
                .isPresent()) {
            throw new ConditionsNotMetException("Пользователь находиться в черном списке и не может подписаться");
        }
    }
}
