package org.edu_sharing.kafka.notification.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.kafka.notification.events.data.Collection;

@Getter
@SuperBuilder
@JsonTypeName("AddToCollectionMessage")
public class AddToCollectionEventDTO extends NodeBaseEventDTO {
    /**
     * the collection the node has been added to
     */
    private Collection collection;
}