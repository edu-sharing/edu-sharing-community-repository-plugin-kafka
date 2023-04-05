package org.edu_sharing.config;

import org.edu_sharing.kafka.notification.events.NotificationEventDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

public class KafkaConfig {


    @Bean
    public KafkaListenerContainerFactory<?> batchFactory(ConsumerFactory<String, NotificationEventDTO> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEventDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        return factory;
    }
}
