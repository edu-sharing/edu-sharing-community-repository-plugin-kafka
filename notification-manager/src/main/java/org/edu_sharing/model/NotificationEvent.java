package org.edu_sharing.model;

import lombok.*;
import org.edu_sharing.kafka.notification.events.data.Status;
import org.edu_sharing.kafka.notification.events.data.UserInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collation = "notification")
public abstract class NotificationEvent {
    @Id
    private String id;
    private Date timestamp;
    private UserInfo creator;
    private UserInfo receiver;
    private Status status;
}

