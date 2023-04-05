package org.edu_sharing.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.kafka.notification.events.NotificationEventDTO;
import org.edu_sharing.kafka.notification.events.data.NodeData;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collation = "notification")
public abstract class NodeBaseEvent extends NotificationEvent {
    private NodeData node;
}

