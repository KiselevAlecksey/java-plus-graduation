package ru.practicum.service;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface UserEventHandler {
    void handle(UserActionProto event);
}
