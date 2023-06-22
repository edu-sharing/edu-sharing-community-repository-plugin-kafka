package org.edu_sharing.notification.repository;

import org.edu_sharing.kafka.notification.events.data.Status;
import org.edu_sharing.notification.model.NotificationEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

// TODO BaseMessage -> NotificationEvent
public interface NotificationRepository extends MongoRepository<NotificationEvent, String>, CustomNotificationRepository {
    Page<NotificationEvent> findAllByCreatorId(String id, Pageable pageable);

   List<NotificationEvent> findAllByTimestampAfterAndStatus(Date newerThan, Status status);

}
