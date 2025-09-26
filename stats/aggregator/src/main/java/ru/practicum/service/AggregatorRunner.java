package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AggregatorRunner implements CommandLineRunner {
    private final AggregatorStarter aggregatorStarter;

    @Override
    public void run(String... args) throws Exception {
        aggregatorStarter.start();
    }
}
