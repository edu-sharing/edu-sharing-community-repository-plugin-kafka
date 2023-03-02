package org.edu_sharing.repository;

import org.edu_sharing.messages.BaseMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

// TODO BaseMessage -> NotificationEvent
public interface NotificationRepository extends MongoRepository<BaseMessage, String>, CustomNotificationRepository {
    Page<BaseMessage> findAllByCreatorId(String id, Pageable pageable);
}
