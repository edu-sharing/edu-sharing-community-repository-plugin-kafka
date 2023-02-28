package org.edu_sharing.plugin_kafka.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Getter
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "_class",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InviteMessage.class, name="InviteMessage"),
        @JsonSubTypes.Type(value = NodeIssueMessage.class, name="NodeIssueMessage"),
        @JsonSubTypes.Type(value = WorkflowMessage.class, name="WorkflowMessage"),
})
public abstract class BaseMessage {
    @JsonProperty("_id")
    private String id;
    private Date timestamp;
    private UserInfo creator;
    private UserInfo receiver;
    private Status status;
}

