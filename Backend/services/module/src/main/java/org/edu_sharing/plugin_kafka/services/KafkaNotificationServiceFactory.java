package org.edu_sharing.plugin_kafka.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.edu_sharing.service.factory.ServiceFactory;
import org.edu_sharing.service.notification.NotificationService;
import org.edu_sharing.spring.ApplicationContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class KafkaNotificationServiceFactory implements ServiceFactory {


    private final KafkaNotificationService notificationService;

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
