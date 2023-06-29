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
@TypeAlias("InviteSafeEvent")
@Document(collection = "notification")
public class InviteSafeEvent extends NodeBaseEvent {

    public InviteSafeEvent(String id, Date timestamp, String creatorId, String receiverId, Status status, NodeData node, String name, String userComment, List<String> permissions) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.name = name;
        this.userComment = userComment;
        this.permissions = permissions;
    }

    private String name;
    private  String userComment;
    @Singular
    private List<String> permissions;
}