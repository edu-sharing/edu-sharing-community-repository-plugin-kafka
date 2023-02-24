package org.edu_sharing.plugin_kafka.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonTypeName("WorkflowMessage")
public class WorkflowMessage extends BaseMessage {
    private String workflowStatus;
    private  String userComment;
}