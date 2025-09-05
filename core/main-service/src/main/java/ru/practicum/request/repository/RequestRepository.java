package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.model.Event;
import ru.practicum.request.enums.RequestState;
import ru.practicum.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterIdAndEventIdAndStatusNotLike(long userId, long eventId, RequestState status);

    List<Request> findAllByRequesterId(long userId);

    List<Request> findAllByRequesterIdAndEventId(Long userId, Long eventId);

    List<Request> findAllByEvent(Event event);
}
