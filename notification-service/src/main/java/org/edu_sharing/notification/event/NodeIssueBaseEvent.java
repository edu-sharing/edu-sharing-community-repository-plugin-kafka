package org.edu_sharing.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.notification.data.NodeData;
import org.edu_sharing.notification.data.Status;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("NodeIssueEvent")
@Document(collection = "notification")
public class NodeIssueBaseEvent extends NodeBaseEvent {

    public NodeIssueBaseEvent(String id, Date timestamp, String creatorId, String receiverId, Status status, NodeData node, String email, String userComment) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.email = email;
        this.userComment = userComment;
    }

    private String email;
    private String userComment;
}