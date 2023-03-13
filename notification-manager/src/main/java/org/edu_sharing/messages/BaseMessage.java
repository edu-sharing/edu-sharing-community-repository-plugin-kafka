package org.edu_sharing.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.edu_sharing.messages.data.Status;
import org.edu_sharing.messages.data.UserInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "_class",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InviteMessage.class, name="InviteMessage"),
        @JsonSubTypes.Type(value = WorkflowMessage.class, name="WorkflowMessage"),
})
@Document(collection = "Notification")
public abstract class BaseMessage {
    @Id
    @JsonProperty("_id")
    private String id;
    private Date timestamp;
    private UserInfo creator;
    private UserInfo receiver;
    private Status status;
}

