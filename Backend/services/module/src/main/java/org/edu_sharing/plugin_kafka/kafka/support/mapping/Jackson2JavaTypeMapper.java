package org.edu_sharing.plugin_kafka.kafka.support.mapping;

import com.fasterxml.jackson.databind.JavaType;
import org.apache.kafka.common.header.Headers;

/**
 * Strategy for setting metadata on messages such that one can create the class that needs
 * to be instantiated when receiving a message.
 */
public interface Jackson2JavaTypeMapper extends ClassMapper {
    /**
     * The precedence for type conversion - inferred from the method parameter or message
     * headers. Only applies if both exist.
     */
    enum TypePrecedence {

        /**
         * The type is inferred from the destination method.
         */
        INFERRED,

        /**
         * The type is obtained from headers.
         */
        TYPE_ID
    }

    void fromJavaType(JavaType javaType, Headers headers);

    JavaType toJavaType(Headers headers);

    TypePrecedence getTypePrecedence();

    /**
     * Set the precedence for evaluating type information in message properties.
     * When using {@code @KafkaListener} at the method level, the framework attempts
     * to determine the target type for payload conversion from the method signature.
     * If so, this type is provided by the {@code MessagingMessageListenerAdapter}.
     * <p> By default, if the type is concrete (not abstract, not an interface), this will
     * be used ahead of type information provided in the {@code __TypeId__} and
     * associated headers provided by the sender.
     * <p> If you wish to force the use of the  {@code __TypeId__} and associated headers
     * (such as when the actual type is a subclass of the method argument type),
     * set the precedence to {@link Jackson2JavaTypeMapper.TypePrecedence#TYPE_ID}.
     * @param typePrecedence the precedence.
     */
    default void setTypePrecedence(TypePrecedence typePrecedence) {
        throw new UnsupportedOperationException("This mapper does not support this method");
    }

    void addTrustedPackages(String... packages);

    /**
     * Remove the type information headers.
     * @param headers the headers.
     */
    default void removeHeaders(Headers headers) {
        // NOSONAR
    }
}
