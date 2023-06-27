package org.edu_sharing.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.notification.data.Status;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification")
public class NotificationEvent {
    @Id
    private String id;
    private Date timestamp;
    private String creatorId;
    private String receiverId;
    private Status status = Status.NEW;
}

