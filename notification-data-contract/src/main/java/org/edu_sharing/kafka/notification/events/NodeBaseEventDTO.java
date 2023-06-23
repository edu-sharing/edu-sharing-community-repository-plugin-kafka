package org.edu_sharing.kafka.notification.events;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.kafka.notification.events.data.NodeData;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class NodeBaseEventDTO extends NotificationEventDTO {
    private NodeData node;
}

