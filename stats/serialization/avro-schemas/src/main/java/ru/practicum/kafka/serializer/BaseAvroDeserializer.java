package ru.practicum.kafka.serializer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private Schema schema;

    public BaseAvroDeserializer(Schema schema) {
        this.schema = schema;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            DatumReader<T> datumReader = new SpecificDatumReader<>(schema);
            BinaryDecoder decoder = decoderFactory.binaryDecoder(in, null);
            return datumReader.read(null, decoder);
        } catch (IOException e) {
            throw new SerializationException("Ошибка десериализации данных для топика [" + topic + "]", e);
        }
    }
}
