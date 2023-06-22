package org.edu_sharing.plugin_kafka.notification;

import lombok.RequiredArgsConstructor;
import org.edu_sharing.service.factory.ServiceFactory;
import org.edu_sharing.service.notification.NotificationService;

@RequiredArgsConstructor
public class KafkaNotificationServiceFactory implements ServiceFactory {


    private final NotificationService notificationService;

    @Override
    public NotificationService getServiceByAppId(String appId) {
        return notificationService;
        //throw new NotImplementedException("getServiceByAppId is not supported for KafkaNotificationServiceFactory");
    }

    @Override
    public NotificationService getLocalService() {
        return notificationService;
    }
}
