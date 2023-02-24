package org.edu_sharing.plugin_kafka.kafka.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;

@RequiredArgsConstructor
public class JsonSerializer<T> implements Serializer<T> {
    private final ObjectMapper writer;

    @Override
    public byte[] serialize(String topic, T data) {
        if(data == null){
            return null;
        }

        try {
            return writer.writeValueAsBytes(data);
        } catch (IOException ex) {
            throw new SerializationException("Can't serialize data [" + data + "] for topic [" + topic + "]", ex);
        }
    }
}
