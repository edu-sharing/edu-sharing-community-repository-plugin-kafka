package org.edu_sharing.plugin_kafka.services;

import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.plugin_kafka.kafka.KafkaTemplate;
import org.edu_sharing.plugin_kafka.config.KafkaSettings;
import org.edu_sharing.plugin_kafka.kafka.SendResult;
import org.edu_sharing.plugin_kafka.messages.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
//@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KafkaNotificationService {

    private final KafkaSettings settings;
    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;

    @Autowired
    public KafkaNotificationService(KafkaSettings settings, KafkaTemplate<String, BaseMessage> kafkaTemplate) {
        this.settings = settings;
        this.kafkaTemplate = kafkaTemplate;

        send(InviteMessage.builder()
                .id(UUID.randomUUID().toString())
                .creator(UserInfo.builder()
                        .id("1")
                        .displayName("Max Mustermann")
                        .build())
                .receiver(UserInfo.builder()
                        .id("2")
                        .displayName("Susi Mustermann")
                        .build())
                .node(Node.builder()
                        .property("Title", "Mein Titel")
                        .property("downloadUrl", "https://edu-sharing.com")
                        .build())
                .status(Status.unread)
                .timestamp(DateTime.now().toDate())
                .userComment("Test invitation")
                .build());
    }



    public CompletableFuture<SendResult<String, BaseMessage>> send(BaseMessage baseMessage) {
        return kafkaTemplate.send(settings.getNotificationTopic(), baseMessage);
    }

}
