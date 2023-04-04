package org.edu_sharing.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import org.edu_sharing.messages.data.Collection;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Notification")
@TypeAlias("AddToCollectionMessage")
@JsonTypeName("AddToCollectionMessage")
public class AddToCollectionMessage extends NodeBaseMessage {
    /**
     * the collection the node has been added to
     */
    private Collection collection;
}