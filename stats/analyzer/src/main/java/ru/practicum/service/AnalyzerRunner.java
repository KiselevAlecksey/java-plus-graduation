package ru.practicum.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AnalyzerRunner implements CommandLineRunner {
    private final List<EventProcessor> processors;
    private final List<Thread> processorThreads = new ArrayList<>();

    public AnalyzerRunner(@Qualifier("userEventProcessor") EventProcessor userProcessor,
                                @Qualifier("similarityEventProcessor") EventProcessor similarityProcessor) {
        this.processors = List.of(userProcessor, similarityProcessor);
    }

    @Override
    public void run(String... args) throws Exception {
        for (EventProcessor processor : processors) {
            Thread thread = new Thread((Runnable) processor);
            thread.setName("processor-" + processor.getProcessorType());
            thread.start();
            processorThreads.add(thread);
        }
    }

    @PreDestroy
    public void stopProcessors() {
        for (Thread thread : processorThreads) {
            thread.interrupt();
        }
    }
}
