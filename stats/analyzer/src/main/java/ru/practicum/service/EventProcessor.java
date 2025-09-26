package ru.practicum.service;

public interface EventProcessor {
    void run();
    String getProcessorType();
}
