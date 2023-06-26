package org.edu_sharing.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.GregorianCalendar;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationManager notificationManager;
    private final NotificationHandler notificationHandler;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyNotifications() {
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_WEEK, -1);
        log.info("send daily notifications");
        notificationHandler.handlePendingNotification(cal.getTime());
    }

    @Scheduled(cron = "0 0 8 * * MON")
    public void sendWeeklyNotifications() {
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_WEEK, -7);
        log.info("send weekly notifications");
        notificationHandler.handlePendingNotification(cal.getTime());
    }
}
