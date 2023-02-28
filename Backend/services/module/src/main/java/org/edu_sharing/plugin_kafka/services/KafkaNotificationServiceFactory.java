package org.edu_sharing.plugin_kafka.services;

import org.apache.commons.lang3.NotImplementedException;
import org.edu_sharing.service.factory.ServiceFactory;
import org.edu_sharing.service.notification.NotificationService;
import org.edu_sharing.spring.ApplicationContextFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("notificationServiceFactory")
public class KafkaNotificationServiceFactory implements ServiceFactory {

    public KafkaNotificationServiceFactory(){

    }

    @Override
    public NotificationService getServiceByAppId(String appId) {
        throw new NotImplementedException("getServiceByAppId is not supported for KafkaNotificationServiceFactory");
    }

    @Override
    public NotificationService getLocalService() {
        return ApplicationContextFactory.getApplicationContext().getBean(KafkaNotificationService.class);
    }
}
