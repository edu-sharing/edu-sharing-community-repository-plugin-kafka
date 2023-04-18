package org.edu_sharing.kafka.notification.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.edu_sharing.kafka.notification.events.data.Collection;

@Getter
@Jacksonized
@SuperBuilder
public class AddToCollectionEventDTO extends NodeBaseEventDTO {
    /**
     * the collection the node has been added to
     */
    private Collection collection;
}