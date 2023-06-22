package org.edu_sharing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.kafka.notification.events.NotificationEventDTO;
import org.edu_sharing.kafka.user.UserDataDTO;
import org.edu_sharing.notification.NotificationHandler;
import org.edu_sharing.notification.model.NotificationEvent;
import org.edu_sharing.userData.UserDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaListeners {
    private final NotificationHandler notificationHandler;
    private final UserDataService userDataService;

    @Value("${kafka.topics.notification}")
    private String notificationTopic;

    @Value("${kafka.topics.userdata}")
    private String userDataTopic;

    @KafkaListener(topics = "${kafka.topics.notification}")
    void notificationListener(List<NotificationEventDTO> messages) {
        log.info("received {} messages on topic {}", messages.size(), notificationTopic);
        List<NotificationEvent> notifications = messages.stream()
                .map(KafkaListeners::mapNotification)
                .collect(Collectors.toList());

        notificationHandler.handleIncomingNotifications(notifications);
    }

    private static NotificationEvent mapNotification(NotificationEventDTO message) {
        return new ObjectMapper().convertValue(message, NotificationEvent.class);
    }

    @KafkaListener(topics="${kafka.topics.userdata}")
    void userDataListener(@Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                          @Payload List<UserDataDTO> message){
        log.info("received {} messages on topic {}", message.size(), userDataTopic);
        userDataService.setUserData(keys, message);
    }
}
