package org.edu_sharing.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.kafka.notification.events.NotificationEventDTO;
import org.edu_sharing.kafka.notification.events.data.Status;
import org.edu_sharing.notification.model.*;
import org.edu_sharing.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationManager {

    private final KafkaTemplate<String, NotificationEventDTO> kafkaTemplate;
    private final NotificationRepository notificationRepository;


    @Value("${kafka.topics.notification}")
    private String notificationTopic;

    public void publishNotificationToKafka(NotificationEventDTO message) {
        kafkaTemplate.send(notificationTopic, message.getId(), message);
    }

    public void saveNotification(NotificationEvent notificationEvent) {
        notificationRepository.save(notificationEvent);
    }

    public Slice<NotificationEvent> getNotificationsByCreatorId(String creatorId, Pageable paging) {
        return notificationRepository.findAllByCreatorId(creatorId, paging);
    }

    public Slice<NotificationEvent> getAllNotifications(Pageable paging){
        return notificationRepository.findAll(paging);
    }

    public List<NotificationEvent> getAllNotifications(Date newerThan, Status status){
        return notificationRepository.findAllByTimestampAfterAndStatus(newerThan, status);
    }

    public NotificationEvent setStatus(String id, Status status) {
        NotificationEvent notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No Message for " + id + "found!"));

        notification.setStatus(status);
        notificationRepository.save(notification);
        return notification;
    }

    public  void deleteNotification(String id) {
        notificationRepository.deleteById(id);
    }

    public  void removeUserName(String userId){
        notificationRepository.removeUserName(userId);
    }

}
