package ru.practicum.service;

import org.apache.avro.specific.SpecificRecord;

public interface EventHandler {
    void handle(SpecificRecord event);
}
