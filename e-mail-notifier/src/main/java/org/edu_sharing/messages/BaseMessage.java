package org.edu_sharing.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.Date;


@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "_class",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InviteMessage.class, name="InviteMessage"),
        @JsonSubTypes.Type(value = WorkflowMessage.class, name="WorkflowMessage"),
})
public abstract class BaseMessage {
    private String id;
    private Date timestamp;
    private UserInfo creator;
    private UserInfo receiver;
    private Status status;
}

