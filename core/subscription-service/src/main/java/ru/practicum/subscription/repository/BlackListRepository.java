package ru.practicum.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.subscription.model.BlackList;

import java.util.List;
import java.util.Optional;

public interface BlackListRepository extends JpaRepository<BlackList, Long> {

    List<BlackList> findAllByUserId(Long userId);

    @Query("select bl from BlackList as bl where bl.userId = :userId and bl.blackList = :blackListId")
    Optional<BlackList> findByUserIdAndBlockUser(Long userId, Long blackListId);
}
