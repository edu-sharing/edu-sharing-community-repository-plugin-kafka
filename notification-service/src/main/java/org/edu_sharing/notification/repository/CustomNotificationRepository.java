package org.edu_sharing.notification.repository;

import com.mongodb.client.result.UpdateResult;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.event.NotificationEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomNotificationRepository {
    Page<NotificationEvent> findAll(String receiverId, List<Status> statusList, Pageable paging);

    UpdateResult updateStatusByReceiverId(String receiverId, List<Status> oldStatus, Status newStatus);
}
