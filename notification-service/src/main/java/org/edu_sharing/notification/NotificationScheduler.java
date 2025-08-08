package org.edu_sharing.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.userData.NotificationInterval;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationHandler notificationHandler;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyNotifications() {
        log.info("send daily notifications");
        notificationHandler.handlePendingNotification(NotificationInterval.daily);
    }

    @Scheduled(cron = "0 0 8 * * MON")
    public void sendWeeklyNotifications() {
        log.info("send weekly notifications");
        notificationHandler.handlePendingNotification(NotificationInterval.weekly);
    }
}
