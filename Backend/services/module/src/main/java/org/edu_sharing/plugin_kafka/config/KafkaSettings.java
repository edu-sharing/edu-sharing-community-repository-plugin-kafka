package org.edu_sharing.plugin_kafka.config;

import com.fasterxml.jackson.databind.JsonSerializer;
import lombok.Data;
import org.edu_sharing.lightbend.ConfigurationProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "kafka")
public class KafkaSettings {
    String notificationTopic = "notification";
    String userDataTopic = "userdata";
    List<String> servers = Collections.singletonList("kafka:9093");
    Duration closeTimeout = Duration.ofSeconds(5);
    String notificationServiceHost;
}
