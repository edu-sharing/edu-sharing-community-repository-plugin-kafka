package org.edu_sharing.kafka.notification.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonTypeName("NodeIssueMessage")
public class NodeIssueEvent extends NodeBaseEvent {
    private  String reason;
    private  String userComment;
}