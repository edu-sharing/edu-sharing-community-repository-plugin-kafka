package org.edu_sharing.notification.repository;

import org.edu_sharing.notification.model.NotificationEvent;
import org.edu_sharing.service.notification.events.data.Status;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

// TODO BaseMessage -> NotificationEvent
public interface NotificationRepository extends MongoRepository<NotificationEvent, String>, CustomNotificationRepository {
    List<NotificationEvent> findAllByTimestampAfterAndStatus(Date newerThan, Status status);

}
