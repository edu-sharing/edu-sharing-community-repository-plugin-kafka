package org.edu_sharing.plugin_kafka.config;

import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.edu_sharing.kafka.notification.event.NotificationEventDTO;
import org.edu_sharing.kafka.user.UserDataDTO;
import org.edu_sharing.plugin_kafka.kafka.KafkaAdmin;
import org.edu_sharing.plugin_kafka.kafka.KafkaProducerFactory;
import org.edu_sharing.plugin_kafka.kafka.KafkaTemplate;
import org.edu_sharing.plugin_kafka.kafka.TopicBuilder;
import org.edu_sharing.plugin_kafka.kafka.support.serializer.JsonSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
public class KafkaConfig implements ApplicationContextAware {

    @Autowired
    KafkaSettings kafkaSettings;


    @Setter
    private ApplicationContext applicationContext;


    private Map<String, Object> kafkaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", kafkaSettings.getServers()));
        return props;
    }

    @Bean
    public KafkaAdmin admin() {
        KafkaAdmin kafkaAdmin = new KafkaAdmin(kafkaProperties());
        kafkaAdmin.setApplicationContext(applicationContext);
        kafkaAdmin.initialize();
        return kafkaAdmin;
    }

    @Bean
    public NewTopic usersTopic() {
        return TopicBuilder.name("userdata")
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(-1))
                .compact()
                .build();
    }

    @Bean
    public NewTopic notificationTopic(){
        return TopicBuilder
                .name("notification")
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(1000*60*60*24))
                .compact()
                .build();
    }

    @NotNull
    private <K, V> KafkaTemplate<K, V> createKafkaTemplate(Supplier<Serializer<K>> keySerializerSupplier, Supplier<Serializer<V>> valueSerializerSupplier, Supplier<String> topic) {
        KafkaProducerFactory<K, V> producerFactory = new KafkaProducerFactory<>(kafkaProperties(), keySerializerSupplier, valueSerializerSupplier);
        return new KafkaTemplate<>(producerFactory, kafkaSettings::getCloseTimeout, topic);
    }

    @Bean
    public KafkaTemplate<String, NotificationEventDTO> kafkaNotificationTemplate() {
        return createKafkaTemplate(StringSerializer::new, JsonSerializer::new, kafkaSettings::getNotificationTopic);
    }


    @Bean
    public KafkaTemplate<String, UserDataDTO> kafkaUserTemplate() {
        return createKafkaTemplate(StringSerializer::new, JsonSerializer::new, kafkaSettings::getUserDataTopic);
    }

}
