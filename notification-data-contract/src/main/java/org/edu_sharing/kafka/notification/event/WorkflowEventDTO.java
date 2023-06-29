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
public class WorkflowEventDTO extends NodeBaseEventDTO {
    public WorkflowEventDTO(String id, Date timestamp, String creatorId, String receiverId, StatusDTO status, NodeDataDTO node, String workflowStatus, String userComment) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.workflowStatus = workflowStatus;
        this.userComment = userComment;
    }

    private String workflowStatus;
    private String userComment;
}