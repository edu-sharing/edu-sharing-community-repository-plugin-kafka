package org.edu_sharing.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

@Data
@JsonTypeName("WorkflowMessage")
public class WorkflowMessage extends BaseMessage {
    private String workflowStatus;
    private  String userComment;
}