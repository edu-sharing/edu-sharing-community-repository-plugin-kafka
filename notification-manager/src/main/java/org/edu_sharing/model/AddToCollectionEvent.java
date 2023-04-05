package org.edu_sharing.model;

import lombok.*;
import org.edu_sharing.kafka.notification.events.data.Collection;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("AddToCollectionMessage")
@Document(collation = "notification")
public class AddToCollectionEvent extends NodeBaseEvent {
    /**
     * the collection the node has been added to
     */
    private Collection collection;
}