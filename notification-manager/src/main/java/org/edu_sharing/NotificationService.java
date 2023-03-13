package org.edu_sharing;

import lombok.AllArgsConstructor;
import org.edu_sharing.messages.BaseMessage;
import org.edu_sharing.messages.data.Status;
import org.edu_sharing.repository.NotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class NotificationService {

    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;
    private final NotificationRepository notificationRepository;

    private final MongoTemplate mongoTemplate;

    public void sendNotification(BaseMessage message) {
        kafkaTemplate.send(AppConstants.TOPIC_NAME_NOTIFICATION, message.getId(), message);
    }

    public Slice<BaseMessage> getNotificationsByCreatorId(String creatorId, Pageable paging) {
        return notificationRepository.findAllByCreatorId(creatorId, paging);
    }

    public Slice<BaseMessage> getAllNotifications(Pageable paging){
        return notificationRepository.findAll(paging);
    }

    public BaseMessage setStatus(String id, Status status) {
        BaseMessage notification = notificationRepository.findById(id)
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
