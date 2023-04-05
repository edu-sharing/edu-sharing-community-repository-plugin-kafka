package org.edu_sharing.kafka.notification.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonTypeName("WorkflowMessage")
public class WorkflowEventDTO extends NodeBaseEventDTO {
    private String workflowStatus;
    private  String userComment;
}