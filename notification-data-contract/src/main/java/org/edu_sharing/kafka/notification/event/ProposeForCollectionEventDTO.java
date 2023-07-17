package org.edu_sharing.kafka.notification.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.edu_sharing.kafka.notification.data.CollectionDTO;
import org.edu_sharing.kafka.notification.data.NodeDataDTO;
import org.edu_sharing.kafka.notification.data.StatusDTO;

import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProposeForCollectionEventDTO extends NodeBaseEventDTO {
    public ProposeForCollectionEventDTO(String id, Date timestamp, String creatorId, String receiverId, StatusDTO status, NodeDataDTO node, CollectionDTO collection) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.collection = collection;
    }

    /**
     * the collection the node has been added to
     */
    private CollectionDTO collection;
}