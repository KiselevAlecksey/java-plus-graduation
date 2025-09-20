package ru.practicum.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@RequiredArgsConstructor
public class RequestScopeObject extends ScopeObject {
}

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
abstract class ScopeObject {
    String requestId;
    String operationId;
}