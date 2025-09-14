package ru.practicum.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.enums.RequestState;

import java.time.LocalDateTime;
import java.util.Objects;

@ToString
@Getter
@Setter
public class Request {
    Long id;

    Long event;

    private LocalDateTime created;

    private Long requester;

    RequestState status;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Request request = (Request) object;
        return Objects.equals(id, request.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
