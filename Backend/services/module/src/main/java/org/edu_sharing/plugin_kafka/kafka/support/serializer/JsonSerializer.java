package org.edu_sharing.plugin_kafka.kafka.support.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.edu_sharing.plugin_kafka.kafka.support.JacksonUtils;
import org.edu_sharing.plugin_kafka.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.edu_sharing.plugin_kafka.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.util.Assert;

import java.io.IOException;

public class JsonSerializer<T> implements Serializer<T> {

    protected final ObjectMapper objectMapper; // NOSONAR
    protected boolean addTypeInfo = true; // NOSONAR

    private final ObjectWriter writer;
    protected Jackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper(); // NOSONAR



    public JsonSerializer() {
        this((JavaType) null, JacksonUtils.enhancedObjectMapper());
    }
    public JsonSerializer(TypeReference<? super T> targetType) {
        this(targetType, JacksonUtils.enhancedObjectMapper());
    }
    public JsonSerializer(ObjectMapper objectMapper) {
        this((JavaType) null, objectMapper);
    }

    public JsonSerializer(TypeReference<? super T> targetType, ObjectMapper objectMapper) {
        this(targetType == null ? null : objectMapper.constructType(targetType.getType()), objectMapper);
    }

    public JsonSerializer(JavaType targetType, ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "'objectMapper' must not be null.");
        this.objectMapper = objectMapper;
        this.writer = objectMapper.writerFor(targetType);
    }
    @Override
    public byte[] serialize(String topic, Headers headers, T data) {
        if (data == null) {
            return null;
        }
        if (this.addTypeInfo && headers != null) {
            this.typeMapper.fromJavaType(this.objectMapper.constructType(data.getClass()), headers);
        }

        return serialize(topic, data);
    }

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
