package org.edu_sharing.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Notification")
@TypeAlias("WorkflowMessage")
@JsonTypeName("WorkflowMessage")
public class WorkflowMessage extends NodeBaseMessage {
    private String workflowStatus;
    private  String userComment;
}