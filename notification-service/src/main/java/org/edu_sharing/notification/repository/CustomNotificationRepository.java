package org.edu_sharing.notification.repository;

import org.edu_sharing.notification.model.NotificationEvent;
import org.edu_sharing.service.notification.events.data.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomNotificationRepository {
    Page<NotificationEvent> findAll(String receiverId, List<Status> statusList, Pageable paging);
}
