package org.edu_sharing.kafka.notification.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.kafka.notification.events.data.Status;

import java.util.Date;

@Getter
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "_class",
        visible = true)
@JsonSubTypes({
        // also register it in the @NotificationEventEnum
        @JsonSubTypes.Type(value = AddToCollectionEventDTO.class, name="AddToCollectionEvent"),
        @JsonSubTypes.Type(value = CommentEventDTO.class, name="CommentEvent"),
        @JsonSubTypes.Type(value = InviteEventDTO.class, name="InviteEvent"),
        @JsonSubTypes.Type(value = NodeIssueEventDTO.class, name="NodeIssueEvent"),
        @JsonSubTypes.Type(value = RatingEventDTO.class, name="RatingEvent"),
        @JsonSubTypes.Type(value = WorkflowEventDTO.class, name="WorkflowEvent"),
        @JsonSubTypes.Type(value = MetadataSuggestionEventDTO.class, name="MetadataSuggestionEvent"),
})
public abstract class NotificationEventDTO {
    @JsonProperty("_id")
    private String id;
    private Date timestamp;
    private String creatorId;
    private String receiverId;
    private Status status;
}

