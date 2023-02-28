package org.edu_sharing.plugin_kafka.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.edu_sharing.plugin_kafka.kafka.KafkaProducerFactory;
import org.edu_sharing.plugin_kafka.kafka.KafkaTemplate;
import org.edu_sharing.plugin_kafka.messages.BaseMessage;
import org.edu_sharing.plugin_kafka.kafka.support.JsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Autowired
    KafkaSettings kafkaSettings;

    private Map<String, Object> kafkaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", kafkaSettings.getServers()));
        return props;
    }

    @Bean
    public KafkaProducerFactory<String, BaseMessage> notificationProducer() {
        return new KafkaProducerFactory<>(kafkaProperties(),
                StringSerializer::new,
                () -> new JsonSerializer<>(JsonMapper.builder()
                        .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .build()));
    }

    @Bean
    public KafkaTemplate<String, BaseMessage> kafkaNotificationTemplate(KafkaProducerFactory<String, BaseMessage> kafkaProducerFactory) {
        return new KafkaTemplate<>(kafkaProducerFactory, kafkaSettings::getCloseTimeout);
    }
}