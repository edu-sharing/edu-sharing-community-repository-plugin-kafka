package org.edu_sharing.kafka.notification.events;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.kafka.notification.events.data.NodeData;

@Getter
@SuperBuilder
public abstract class NodeBaseEventDTO extends NotificationEventDTO {
    private NodeData node;
}

