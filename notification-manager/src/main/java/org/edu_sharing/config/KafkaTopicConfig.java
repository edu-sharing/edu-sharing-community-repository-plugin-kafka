package org.edu_sharing.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.edu_sharing.AppConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {


    /**
     * The NewTopic bean causes the topic to be created on the broker; it is not needed if the topic already exists.
     */
    @Bean
    public NewTopic notificationTopic(){
        return TopicBuilder.name(AppConstants.TOPIC_NAME_NOTIFICATION)
                .build();
    }
}
