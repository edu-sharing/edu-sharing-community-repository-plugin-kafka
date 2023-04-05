package org.edu_sharing.repository;

import org.edu_sharing.model.NotificationEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

// TODO BaseMessage -> NotificationEvent
public interface NotificationRepository extends MongoRepository<NotificationEvent, String>, CustomNotificationRepository {
    Page<NotificationEvent> findAllByCreatorId(String id, Pageable pageable);
}
