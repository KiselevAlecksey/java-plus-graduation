package ru.practicum.kafka.serializer;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class SimilarityActionDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {
    public SimilarityActionDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
