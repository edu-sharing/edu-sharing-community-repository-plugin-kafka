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
public class CommentEventDTO extends NodeBaseEventDTO {
    public CommentEventDTO(String id, Date timestamp, String creatorId, String receiverId, StatusDTO status, NodeDataDTO node, String commentContent, String commentReference, String event) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.commentContent = commentContent;
        this.commentReference = commentReference;
        this.event = event;
    }

    private  String commentContent;

    /**
     * the id this comment refers to, if any
     */
    private String commentReference;
    private String event;
}