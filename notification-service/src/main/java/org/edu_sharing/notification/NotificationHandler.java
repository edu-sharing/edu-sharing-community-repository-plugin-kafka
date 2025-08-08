package org.edu_sharing.notification;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.event.NotificationEvent;
import org.edu_sharing.service.NotificationService;
import org.edu_sharing.userData.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHandler {

    private final NotificationManager notificationManager;
    private final List<NotificationService> notificationServices;
    private final UserDataService userDataService;

    @PostConstruct
    public void resendFailedNotificationsOnStartup() {
        handlePendingNotification(NotificationInterval.immediately);
    }


    @EventListener
    public void handlePendingNotificationOnAddedUser(UserDataAddedEvent addedEvent) {
        UserData userData = addedEvent.getUserData();

        List<NotificationEvent> notificationEvents = notificationManager.getAllNotifications(userData.getId(), List.of(Status.PENDING));
        List<NotificationEvent> notificationEventsToSend = new ArrayList<>(notificationEvents.size());
        for (NotificationEvent notificationEvent : notificationEvents) {
            NotificationInterval notificationInterval = userData.getNotificationInterval(notificationEvent);
            switch (notificationInterval) {
                case disabled -> notificationEvent.setStatus(Status.IGNORED);
                case immediately -> notificationEventsToSend.add(notificationEvent);
            }
        }

        if (notificationEventsToSend.isEmpty()) {
            return;
        }

        notificationServices.forEach(x -> x.send(notificationEventsToSend));
        notificationManager.saveAllNotifications(notificationEventsToSend);
    }

    @EventListener
    public void handlePendingNotificationOnDeletedUser(UserDataDeletedEvent addedEvent) {
        UserData userData = addedEvent.getOldUserData();

        List<NotificationEvent> notificationEvents = notificationManager.getAllNotifications(userData.getId(), List.of(Status.PENDING));
        if (notificationEvents.isEmpty()) {
            return;
        }

        notificationEvents.forEach(x -> x.setStatus(Status.IGNORED));
        notificationManager.saveAllNotifications(notificationEvents);
    }

    @EventListener
    public void handlePendingNotificationOnChangedUser(UserDataChangedEvent event) {
        UserData oldUserData = event.getOldUserData();
        UserData newUserData = event.getNewUserData();

        if (!Objects.equals(oldUserData.getId(), newUserData.getId())) {
            throw new IllegalArgumentException("Old and new user data must have the same id!");
        }

        List<NotificationEvent> notificationEvents = notificationManager.getAllNotifications(oldUserData.getId(), List.of(Status.PENDING));
        List<NotificationEvent> notificationEventsToSend = new ArrayList<>(notificationEvents.size());
        List<NotificationEvent> notificationEventsToSave = new ArrayList<>(notificationEvents.size());
        for (NotificationEvent notificationEvent : notificationEvents) {
            NotificationInterval oldNotificationInterval = oldUserData.getNotificationInterval(notificationEvent);
            NotificationInterval newNotificationInterval = newUserData.getNotificationInterval(notificationEvent);
            if (oldNotificationInterval == newNotificationInterval) {
                log.info("Notification interval of user {} did not change. Ignore notification.", oldUserData.getId());
                continue;
            }

            switch (newNotificationInterval) {
                case disabled -> {
                    notificationEvent.setStatus(Status.IGNORED);
                    notificationEventsToSave.add(notificationEvent);
                }
                case immediately -> {
                    notificationEventsToSend.add(notificationEvent);
                    notificationEventsToSave.add(notificationEvent);
                }
            }
        }

        if (!notificationEventsToSend.isEmpty()) {
            notificationServices.forEach(x -> x.send(notificationEventsToSend));
        }

        if(!notificationEventsToSave.isEmpty()){
            notificationManager.saveAllNotifications(notificationEventsToSave);
        }
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

        Map<String, UserData> userDataMap = userDataService.getUserDataAsMap(notificationEvents.stream().map(NotificationEvent::getReceiverId)
                        .distinct()
                        .toList());

        List<NotificationEvent> disabledEvents = notificationEvents.stream().filter(x -> {
            UserData userData = userDataMap.get(x.getReceiverId());
            return userData == null || userData.getNotificationInterval(x) == NotificationInterval.disabled;
        }).collect(Collectors.toList());

        if(!disabledEvents.isEmpty()) {
            disabledEvents.forEach(x -> x.setStatus(Status.IGNORED));
            notificationManager.saveAllNotifications(disabledEvents);
        }

        if(notificationInterval == NotificationInterval.disabled) {
            return;
        }

        List<NotificationEvent> eventsToSend = notificationEvents.stream().filter(x -> {
            UserData userData = userDataMap.get(x.getReceiverId());
            return userData != null && userData.getNotificationInterval(x) == notificationInterval;
        }).collect(Collectors.toList());

        if(!eventsToSend.isEmpty()) {
            notificationServices.forEach(x -> x.send(eventsToSend));
            notificationManager.saveAllNotifications(eventsToSend);
        }
    }

    public void handleIncomingNotifications(List<NotificationEvent> notificationEvents) {
        notificationManager.saveAllNotifications(notificationEvents);

        handlePendingNotification(NotificationInterval.immediately, notificationEvents);
        List<NotificationEvent> newNotifications = notificationEvents.stream()
                .filter(x -> x.getStatus() == Status.NEW)
                .toList();

        newNotifications.forEach(x -> {
                x.setStatus(Status.PENDING);
        });

        notificationManager.saveAllNotifications(newNotifications);
    }

}
