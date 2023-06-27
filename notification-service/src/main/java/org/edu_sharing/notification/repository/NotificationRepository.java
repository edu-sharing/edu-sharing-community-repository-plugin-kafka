package org.edu_sharing.notification.repository;

import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.event.NotificationEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

// TODO BaseMessage -> NotificationEvent
public interface NotificationRepository extends MongoRepository<NotificationEvent, String>, CustomNotificationRepository {
    List<NotificationEvent> findAllByTimestampAfterAndStatus(Date newerThan, Status status);

}
