package org.edu_sharing.plugin_kafka.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor(onConstructor_ = @Autowired)

public class KafkaProducerFactory<K,V> {

    final Map<String, Object> configs;
    final Supplier<Serializer<K>> keySerializerSupplier;
    final Supplier<Serializer<V>> valueSerializerSupplier;

    public Producer<K, V> createProducer() {
        return new KafkaProducer<>(configs, keySerializerSupplier.get(), valueSerializerSupplier.get());
    }

}
