package org.edu_sharing.notification;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.event.NotificationEvent;
import org.edu_sharing.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationManager {

    private final KafkaTemplate<String, org.edu_sharing.kafka.notification.event.NotificationEventDTO> kafkaTemplate;
    private final NotificationRepository notificationRepository;


    @Value("${kafka.topics.notification}")
    private String notificationTopic;

    public void publishNotificationToKafka(org.edu_sharing.kafka.notification.event.NotificationEventDTO message) {
        kafkaTemplate.send(notificationTopic, message.getId(), message);
    }

    public void saveNotification(NotificationEvent notificationEvent) {
        notificationRepository.save(notificationEvent);
    }


    public Page<NotificationEvent> getAllNotifications(String receiverId, List<Status> statusList, Pageable paging) {
        return notificationRepository.findAll(receiverId, statusList, paging);
    }

    public List<NotificationEvent> getAllNotifications(Date newerThan, Status status) {
        return notificationRepository.findAllByTimestampAfterAndStatus(newerThan, status);
    }

    public NotificationEvent setStatusByNotificationId(String id, Status status) {
        NotificationEvent notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No Message for " + id + "found!"));
        log.info("Status \"{}\" for notifications \"{}\" was updated", status, id);

        notification.setStatus(status);
        notificationRepository.save(notification);
        return notification;
    }

    public void setStatusByReceiverId(String receiverId, List<Status> oldStatusList, Status newStatus) {
        UpdateResult updateResult = notificationRepository.updateStatusByReceiverId(receiverId, oldStatusList,  newStatus);
        log.info("Updated all notification of receiver \"{}\" with status {} to status {}. Result: {}", receiverId, oldStatusList, newStatus, updateResult);
    }

    public void deleteNotification(String id) {
        log.info("Delete notification \"{}\"", id);
        notificationRepository.deleteById(id);
    }

}
