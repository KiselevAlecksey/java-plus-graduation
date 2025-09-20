package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.enums.RequestState;
import ru.practicum.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterAndEventAndStatusNotLike(long userId, long event, RequestState status);

    List<Request> findAllByRequester(long userId);

}
