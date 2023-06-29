package org.edu_sharing.notification.event;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.notification.data.NodeData;
import org.edu_sharing.notification.data.Status;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("InviteEvent")
@Document(collection = "notification")
public class InviteEvent extends NodeBaseEvent {

    public InviteEvent(String id, Date timestamp, String creatorId, String receiverId, Status status, NodeData node, String name, String type, String userComment, List<String> permissions) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.name = name;
        this.type = type;
        this.userComment = userComment;
        this.permissions = permissions;
    }

    private String name;
    private String type;
    private  String userComment;
    @Singular
    private List<String> permissions;
}