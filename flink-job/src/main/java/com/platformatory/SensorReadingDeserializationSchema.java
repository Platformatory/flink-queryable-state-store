package com.platformatory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;

import java.io.IOException;

public class SensorReadingDeserializationSchema extends AbstractDeserializationSchema<SensorReading> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SensorReading deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, SensorReading.class);
    }
}
