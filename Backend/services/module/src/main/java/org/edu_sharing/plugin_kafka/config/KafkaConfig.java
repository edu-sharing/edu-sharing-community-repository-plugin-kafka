package org.edu_sharing.plugin_kafka.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import groovy.text.Template;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.edu_sharing.kafka.notification.events.NotificationEventDTO;
import org.edu_sharing.kafka.user.UserDataDTO;
import org.edu_sharing.plugin_kafka.kafka.*;
import org.edu_sharing.plugin_kafka.kafka.support.serializer.JsonSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
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
        return TopicBuilder.name("Users")
                .compact()
                .build();
    }

    @NotNull
    private <K, V> KafkaTemplate<K, V> createKafkaTemplate(Supplier<Serializer<K>> keySerializerSupplier, Supplier<Serializer<V>> valueSerializerSupplier) {
        KafkaProducerFactory<K, V> producerFactory = new KafkaProducerFactory<>(kafkaProperties(), keySerializerSupplier, valueSerializerSupplier);
        return new KafkaTemplate<>(producerFactory, kafkaSettings::getCloseTimeout, kafkaSettings::getNotificationTopic);
    }

    @Bean
    public KafkaTemplate<String, NotificationEventDTO> kafkaNotificationTemplate() {
        return createKafkaTemplate(StringSerializer::new, JsonSerializer::new);
    }


    @Bean
    public KafkaTemplate<String, UserDataDTO> kafkaUserTemplate() {
        return createKafkaTemplate(StringSerializer::new, JsonSerializer::new);
    }

}
