package org.edu_sharing.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.notification.data.NodeData;
import org.edu_sharing.notification.data.Status;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification")
public class NodeBaseEvent extends NotificationEvent {
    public NodeBaseEvent(String id, Date timestamp, String creatorId, String receiverId, Status status, NodeData node) {
        super(id, timestamp, creatorId, receiverId, status);
        this.node = node;
    }

    private NodeData node;
}

