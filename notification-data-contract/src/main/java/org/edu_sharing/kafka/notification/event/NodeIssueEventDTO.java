package org.edu_sharing.kafka.notification.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.edu_sharing.kafka.notification.data.NodeDataDTO;
import org.edu_sharing.kafka.notification.data.StatusDTO;

import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NodeIssueEventDTO extends NodeIssueBaseEventDTO {
    public NodeIssueEventDTO(String id, Date timestamp, String creatorId, String receiverId, StatusDTO status, NodeDataDTO node, String email, String reason, String userComment) {
        super(id, timestamp, creatorId, receiverId, status, node, email, userComment);
        this.reason = reason;
    }

    private String reason;
}