package org.edu_sharing.plugin_kafka.kafka.support.mapping;

import org.apache.kafka.common.header.Headers;

/**
 * Strategy for setting metadata on messages such that one can create the class
 * that needs to be instantiated when receiving a message.
 */
public interface ClassMapper {
    void fromClass(Class<?> clazz, Headers headers);
    Class<?> toClass(Headers headers);
}
