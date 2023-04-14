package org.edu_sharing.plugin_kafka.kafka.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

public class JacksonMimeTypeModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    public JacksonMimeTypeModule() {
        addSerializer(MimeType.class, new MimeTypeSerializer());
    }

    /**
     * Simple {@link JsonSerializer} extension to represent a {@link MimeType} object in the
     * target JSON as a plain string.
     */
    private static final class MimeTypeSerializer extends JsonSerializer<MimeType> {

        MimeTypeSerializer() {
        }

        @Override
        public void serialize(MimeType value, JsonGenerator generator, SerializerProvider serializers)
                throws IOException {

            generator.writeString(value.toString());
        }

    }
}
