package org.edu_sharing.kafka.notification.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonTypeName("NodeIssueMessage")
public class NodeIssueEventDTO extends NodeBaseEventDTO {
    private  String reason;
    private  String userComment;
}