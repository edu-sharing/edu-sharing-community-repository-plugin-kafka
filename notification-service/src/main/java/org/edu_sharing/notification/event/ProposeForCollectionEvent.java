package org.edu_sharing.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.notification.data.Collection;
import org.edu_sharing.notification.data.NodeData;
import org.edu_sharing.notification.data.Status;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("ProposeForCollectionMessage")
@Document(collection = "notification")
public class ProposeForCollectionEvent extends NodeBaseEvent {

    public ProposeForCollectionEvent(String id, Date timestamp, String creatorId, String receiverId, Status status, NodeData node, Collection collection) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.collection = collection;
    }

    /**
     * the collection the node has been added to
     */
    private Collection collection;
}
