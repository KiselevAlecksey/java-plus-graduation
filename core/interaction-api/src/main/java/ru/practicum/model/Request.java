package ru.practicum.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.enums.RequestState;

import java.time.LocalDateTime;
import java.util.Objects;

@ToString
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Request {
    Long id;

    Long event;

    LocalDateTime created;

    Long requester;

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
