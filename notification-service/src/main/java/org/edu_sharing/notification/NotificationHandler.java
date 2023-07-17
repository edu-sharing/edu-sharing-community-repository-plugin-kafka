package org.edu_sharing.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.event.*;
import org.edu_sharing.service.NotificationService;
import org.edu_sharing.userData.NotificationInterval;
import org.edu_sharing.userData.UserData;
import org.edu_sharing.userData.UserDataRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHandler {

    private final NotificationManager notificationManager;
    private final UserDataRepository userDataRepository;
    private final List<NotificationService> notificationServices;

    public void handlePendingNotification(Date newerThan, NotificationInterval notificationInterval) {
        List<NotificationEvent> notificationEvents = notificationManager.getAllNotifications(newerThan, Status.PENDING);

        List<NotificationEvent> filteredEvents = notificationEvents.stream().filter(x -> {
            UserData userData = userDataRepository.findById(x.getReceiverId()).orElse(new UserData());
            return userData.getNotificationInterval(x) == notificationInterval;
        }).collect(Collectors.toList());

        notificationServices.forEach(x->x.send(filteredEvents));
        filteredEvents.forEach(notificationManager::saveNotification);
    }

    public void handleIncomingNotifications(List<NotificationEvent> notificationEvents) {
        notificationEvents.forEach(notificationManager::saveNotification);


        List<NotificationEvent> filteredEvents = notificationEvents.stream().filter(x -> {
            UserData userData = userDataRepository.findById(x.getReceiverId()).orElse(new UserData());
            return userData.getNotificationInterval(x) == NotificationInterval.immediately;
        }).collect(Collectors.toList());

        notificationServices.forEach(x->x.send(filteredEvents));
        notificationEvents.forEach(x->{
            if(x.getStatus() == Status.NEW){
                x.setStatus(Status.PENDING);
            }
        });

        notificationEvents.forEach(notificationManager::saveNotification);
    }

}
