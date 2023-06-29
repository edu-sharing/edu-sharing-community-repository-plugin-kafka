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
@TypeAlias("CommentEvent")
@Document(collection = "notification")
public class CommentEvent extends NodeBaseEvent {

    public CommentEvent(String id, Date timestamp, String creatorId, String receiverId, Status status, NodeData node, String commentContent, String commentReference, String event) {
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