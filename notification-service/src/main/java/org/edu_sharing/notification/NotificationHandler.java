package org.edu_sharing.notification;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.event.NotificationEvent;
import org.edu_sharing.service.NotificationService;
import org.edu_sharing.userData.NotificationInterval;
import org.edu_sharing.userData.UserData;
import org.edu_sharing.userData.UserDataRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHandler {

    private final NotificationManager notificationManager;
    private final UserDataRepository userDataRepository;
    private final List<NotificationService> notificationServices;

    @PostConstruct
    public void resendFailedNotificationsOnStartup() {
        handlePendingNotification(NotificationInterval.immediately);
    }

    public void handlePendingNotification(NotificationInterval notificationInterval) {
        List<NotificationEvent> notificationEvents = notificationManager.getAllNotifications(Status.PENDING);
        handlePendingNotification(notificationInterval, notificationEvents);
    }

    public void handlePendingNotification(Date newerThan, NotificationInterval notificationInterval) {
        List<NotificationEvent> notificationEvents = notificationManager.getAllNotifications(newerThan, Status.PENDING);
        handlePendingNotification(notificationInterval, notificationEvents);
    }

    private void handlePendingNotification(NotificationInterval notificationInterval, List<NotificationEvent> notificationEvents) {

        Map<String, UserData> userDataMap = notificationEvents.stream().map(NotificationEvent::getReceiverId)
                .distinct()
                .collect(Collectors.toMap(id -> id, id -> userDataRepository.findById(id).orElse(new UserData())));

        List<NotificationEvent> disabledEvents = notificationEvents.stream().filter(x -> {
            UserData userData = userDataMap.get(x.getReceiverId());
            return userData.getNotificationInterval(x) == NotificationInterval.disabled;
        }).collect(Collectors.toList());

        disabledEvents.forEach(x -> x.setStatus(Status.IGNORED));
        notificationManager.saveAllNotifications(disabledEvents);

        List<NotificationEvent> filteredEvents = notificationEvents.stream().filter(x -> {
            UserData userData = userDataMap.get(x.getReceiverId());
            return userData.getNotificationInterval(x) == notificationInterval;
        }).collect(Collectors.toList());

        notificationServices.forEach(x -> x.send(filteredEvents));
        notificationManager.saveAllNotifications(filteredEvents);
    }

    public void handleIncomingNotifications(List<NotificationEvent> notificationEvents) {
        notificationManager.saveAllNotifications(notificationEvents);

        handlePendingNotification(NotificationInterval.immediately, notificationEvents);
        notificationEvents.forEach(x -> {
            if (x.getStatus() == Status.NEW) {
                x.setStatus(Status.PENDING);
            }
        });

        notificationEvents.forEach(notificationManager::saveNotification);
        notificationManager.saveAllNotifications(notificationEvents);
    }

}
