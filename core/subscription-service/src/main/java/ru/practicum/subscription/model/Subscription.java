package ru.practicum.subscription.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Subscription {

    Long userId;

    Set<Subscriber> subscribed;

    Set<BlackList> blackList;

}
