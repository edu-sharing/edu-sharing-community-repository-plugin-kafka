package org.edu_sharing.plugin_kafka.kafka.support.converter;


public class MessageConversionException extends RuntimeException {

    public MessageConversionException(String description) {
        super(description);
    }

    public MessageConversionException(String description, Throwable cause) {
        super(description, cause);
    }
}