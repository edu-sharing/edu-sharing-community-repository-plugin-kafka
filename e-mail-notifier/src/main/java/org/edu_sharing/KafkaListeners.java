package org.edu_sharing;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.messages.BaseMessage;
import org.edu_sharing.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaListeners {
    private final EmailService emailService;

    @KafkaListener(topics = "${kafka.topics.notification}", groupId = "${kafka.groupId}")
    void notificationListener(BaseMessage message) {
        try {
            emailService.send(message);
        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
    }
}
