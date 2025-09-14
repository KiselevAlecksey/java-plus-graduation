package ru.practicum.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.subscription.model.Subscriber;

import java.util.List;
import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    List<Subscriber> findAllByUserId(Long userId);

    void deleteByUserIdAndSubscriber(Long userId, Long subscriber);

    Optional<Subscriber> findByUserIdAndSubscriber(Long userId, Long subscriber);
}
