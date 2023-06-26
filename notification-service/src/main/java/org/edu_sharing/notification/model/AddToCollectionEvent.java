package org.edu_sharing.notification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.service.notification.events.data.Collection;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("AddToCollectionMessage")
@Document(collection = "notification")
public class AddToCollectionEvent extends NodeBaseEvent {
    /**
     * the collection the node has been added to
     */
    private Collection collection;
}