package org.edu_sharing.kafka.notification.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.kafka.notification.events.data.Status;
import org.edu_sharing.kafka.notification.events.data.UserInfo;

import java.util.Date;

@Getter
@SuperBuilder
public abstract class NotificationEvent {
    @JsonProperty("_id")
    private String id;
    private Date timestamp;
    private UserInfo creator;
    private UserInfo receiver;
    private Status status;
}

