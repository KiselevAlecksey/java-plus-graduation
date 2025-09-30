package ru.practicum.service.similarity.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.service.EventHandler;
import ru.practicum.service.GenericEventProcessor;
import ru.practicum.service.client.Client;

@Slf4j
@Component
public class SimilarityEventProcessor extends GenericEventProcessor {
    public SimilarityEventProcessor(
            @Qualifier("similarityClient") Client similarityEventClient,
            @Qualifier("similarityEventHandler") EventHandler similarityEventHandler
    ) {
        super(similarityEventClient, similarityEventHandler, "SIMILARITY_EVENT");
    }
}
