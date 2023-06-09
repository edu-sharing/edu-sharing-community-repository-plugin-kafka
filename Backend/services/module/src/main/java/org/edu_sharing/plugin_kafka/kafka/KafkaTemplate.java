package org.edu_sharing.plugin_kafka.kafka;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.edu_sharing.plugin_kafka.kafka.support.KafkaUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@Slf4j
public class KafkaTemplate<K,V> {
    private final KafkaProducerFactory<K, V> producerFactory;
    private final Supplier<Duration> closeTimeoutSupplier;

    @Getter
    @Setter
    private Supplier<String> defaultTopicSupplier;



    public KafkaTemplate(KafkaProducerFactory<K, V> producerFactory) {
        this(producerFactory, () -> Duration.ofSeconds(5));
    }

    public KafkaTemplate(KafkaProducerFactory<K, V> producerFactory, Supplier<Duration> closeTimeoutSupplier) {
        this(producerFactory, closeTimeoutSupplier, ()->null);
    }

    public KafkaTemplate(KafkaProducerFactory<K, V> producerFactory,  Supplier<Duration> closeTimeoutSupplier, String defaultTopic) {
        this(producerFactory, closeTimeoutSupplier, () -> defaultTopic);
    }

    public KafkaTemplate(KafkaProducerFactory<K, V> producerFactory,  Supplier<Duration> closeTimeoutSupplier, Supplier<String> defaultTopicSupplier) {
        this.producerFactory = producerFactory;
        this.closeTimeoutSupplier = closeTimeoutSupplier;
        this.defaultTopicSupplier = defaultTopicSupplier;
    }

    @PostConstruct
    public void Initialization(){

    }

    /**
     * Send the data to the default topic with no key or partition.
     * @param data The data.
     * @return a Future for the {@link SendResult}.
     */
    public CompletableFuture<SendResult<K, V>> sendDefault(V data) {
        ProducerRecord<K, V> producerRecord = new ProducerRecord<>(defaultTopicSupplier.get(), data);
        return send(producerRecord);
    }

    /**
     * Send the data to the default topic with the provided key and no partition.
     * @param key the key.
     * @param data The data.
     * @return a Future for the {@link SendResult}.
     */
    public CompletableFuture<SendResult<K, V>> sendDefault(K key, V data) {
        ProducerRecord<K, V> producerRecord = new ProducerRecord<>(defaultTopicSupplier.get(), key, data);
        return send(producerRecord);
    }

    /**
     * Send the data to the default topic with the provided key and partition.
     * @param partition the partition.
     * @param key the key.
     * @param data the data.
     * @return a Future for the {@link SendResult}.
     */
    public CompletableFuture<SendResult<K, V>> sendDefault(Integer partition, K key, V data) {
        ProducerRecord<K, V> producerRecord = new ProducerRecord<>(defaultTopicSupplier.get(), partition, key, data);
        return send(producerRecord);
    }

    /**
     * Send the data to the default topic with the provided key and partition.
     * @param partition the partition.
     * @param timestamp the timestamp of the record.
     * @param key the key.
     * @param data the data.
     * @return a Future for the {@link SendResult}.
     */
    public CompletableFuture<SendResult<K, V>> sendDefault(Integer partition, Long timestamp, K key, V data) {
        ProducerRecord<K, V> producerRecord = new ProducerRecord<>(defaultTopicSupplier.get(), partition, timestamp, key, data);
        return send(producerRecord);
    }

    /**
     * Send the data to the provided topic with no key or partition.
     * @param topic the topic.
     * @param data The data.
     * @return a Future for the {@link SendResult}.
     */
    public CompletableFuture<SendResult<K, V>> send(String topic, V data) {
        ProducerRecord<K, V> producerRecord = new ProducerRecord<>(topic, data);
        return send(producerRecord);
    }

    /**
     * Send the data to the provided topic with the provided key and no partition.
     * @param topic the topic.
     * @param key the key.
     * @param data The data.
     * @return a Future for the {@link SendResult}.
     */
    public CompletableFuture<SendResult<K, V>> send(String topic, K key, V data) {
        ProducerRecord<K, V> producerRecord = new ProducerRecord<>(topic, key, data);
        return send(producerRecord);
    }

    /**
     * Send the data to the provided topic with the provided key and partition.
     * @param topic the topic.
     * @param partition the partition.
     * @param key the key.
     * @param data the data.
     * @return a Future for the {@link SendResult}.
     */
    public CompletableFuture<SendResult<K, V>> send(String topic, Integer partition, K key, V data) {
        ProducerRecord<K, V> producerRecord = new ProducerRecord<>(topic, partition, key, data);
        return send(producerRecord);
    }

    /**
     * Send the data to the provided topic with the provided key and partition.
     * @param topic the topic.
     * @param partition the partition.
     * @param timestamp the timestamp of the record.
     * @param key the key.
     * @param data the data.
     * @return a Future for the {@link SendResult}.
     */
    public CompletableFuture<SendResult<K, V>> send(String topic, Integer partition, Long timestamp, K key, V data) {
        ProducerRecord<K, V> producerRecord = new ProducerRecord<>(topic, partition, timestamp, key, data);
        return send(producerRecord);
    }


    /**
     * Send the provided {@link ProducerRecord}.
     * @param record the record.
     * @return a Future for the {@link SendResult}.
     */
    public CompletableFuture<SendResult<K, V>> send(ProducerRecord<K, V> record) {

        final Producer<K, V> producer = producerFactory.createProducer();
        final CompletableFuture<SendResult<K, V>> future = new CompletableFuture<>();

        Future<RecordMetadata> sendFuture = producer.send(record, buildCallback(record, producer, future));

        // May be an immediate failure
        if (sendFuture.isDone()) {
            try {
                sendFuture.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new KafkaException("Interrupted", e);
            } catch (ExecutionException e) {
                throw new KafkaException("Send failed", e.getCause()); // NOSONAR, stack trace
            }
        }

        log.trace("Sent: " + KafkaUtils.format(record));
        return future;
    }

    private Callback buildCallback(ProducerRecord<K, V> producerRecord, Producer<K, V> producer, CompletableFuture<SendResult<K, V>> future) {
        return (metadata, exception) -> {
            try {
                if (exception == null) {
                    future.complete(new SendResult<>(producerRecord, metadata));
                    log.trace("Sent ok: " + KafkaUtils.format(producerRecord) + ", metadata: " + metadata);
                } else {
                    future.completeExceptionally(
                            new KafkaProducerException(producerRecord, "Failed to send", exception));

                    log.debug("Failed to send: " + KafkaUtils.format(producerRecord), exception);
                }
            } finally {
                producer.close(closeTimeoutSupplier.get());
            }
        };
    }

}
