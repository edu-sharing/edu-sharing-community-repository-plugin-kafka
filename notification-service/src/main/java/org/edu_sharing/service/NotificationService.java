package org.edu_sharing.service;

import org.edu_sharing.notification.model.NotificationEvent;

import java.util.List;

public interface NotificationService {
    void send(List<NotificationEvent> event);
//    void send(NotificationEvent event);
}
