package org.edu_sharing.plugin_kafka.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.edu_sharing.plugin_kafka.messages.BaseMessage;

public class KafkaProducerException extends KafkaException {

    private final ProducerRecord<?, ?> producerRecord;

    /**
     * Construct an instance with the provided properties.
     * @param failedProducerRecord the producer record.
     * @param message the message.
     * @param cause the cause.
     */
    public KafkaProducerException(ProducerRecord<?, ?> failedProducerRecord, String message, Throwable cause) {
        super(message, cause);
        this.producerRecord = failedProducerRecord;
    }

    /**
     * Return the failed producer record.
     * @param <K> the key type.
     * @param <V> the value type.
     * @return the record.
     * @since 2.5
     */
    @SuppressWarnings("unchecked")
    public <K, V> ProducerRecord<K, V> getFailedProducerRecord() {
        return (ProducerRecord<K, V>) this.producerRecord;
    }
}
