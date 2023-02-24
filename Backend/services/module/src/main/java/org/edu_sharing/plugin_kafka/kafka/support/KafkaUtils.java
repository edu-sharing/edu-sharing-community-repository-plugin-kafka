package org.edu_sharing.plugin_kafka.kafka.support;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public class KafkaUtils {
    private static Function<ProducerRecord<?, ?>, String> prFormatter = ProducerRecord::toString;

    private static Function<ConsumerRecord<?, ?>, String> crFormatter =
            rec -> rec.topic() + "-" + rec.partition() + "@" + rec.offset();

    /**
     * Format the {@link ConsumerRecord} for logging; default
     * {@code topic-partition@offset}.
     * @param record the record to format.
     * @return the formatted String.
     * @since 2.7.12
     */
    public static String format(ConsumerRecord<?, ?> record) {
        return crFormatter.apply(record);
    }

    /**
     * Format the {@link ProducerRecord} for logging; default
     * {@link ProducerRecord}{@link #toString()}.
     * @param record the record to format.
     * @return the formatted String.
     * @since 2.7.12
     */
    public static String format(ProducerRecord<?, ?> record) {
        return prFormatter.apply(record);
    }
}
