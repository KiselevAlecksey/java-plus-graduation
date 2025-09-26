package ru.practicum.service;

import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;

public interface EventHandler {
    void handle(SpecificRecord event);
}
