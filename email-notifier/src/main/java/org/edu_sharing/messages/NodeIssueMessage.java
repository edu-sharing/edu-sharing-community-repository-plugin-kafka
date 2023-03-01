package org.edu_sharing.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonTypeName("NodeIssueMessage")
public class NodeIssueMessage extends NodeBaseMessage {
    private  String reason;
    private  String userComment;
}