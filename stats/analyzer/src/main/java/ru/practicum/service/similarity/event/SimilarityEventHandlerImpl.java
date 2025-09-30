package ru.practicum.service.similarity.event;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.service.EventHandler;

@Component
@RequiredArgsConstructor
@Qualifier("similarityEventHandler")
public class SimilarityEventHandlerImpl implements EventHandler {
    private final SimilarityRepository similarityRepository;

    @Override
    @Transactional
    public void handle(SpecificRecord event) {
        Similarity similarity = toSimilarity((EventSimilarityAvro) event);
        similarityRepository.save(similarity);
    }

    private Similarity toSimilarity(EventSimilarityAvro event) {
        return Similarity.builder()
                .eventA(event.getEventA())
                .eventB(event.getEventB())
                .similarity(event.getScore())
                .timestamp(event.getTimestamp())
                .build();
    }
}
