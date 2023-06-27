package org.edu_sharing.kafka.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.kafka.notification.data.NodeData;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class NodeBaseEventDTO extends NotificationEventDTO {
    private NodeData node;
}

