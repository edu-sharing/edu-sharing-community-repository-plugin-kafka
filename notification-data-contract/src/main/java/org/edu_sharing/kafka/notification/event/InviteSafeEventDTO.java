package org.edu_sharing.kafka.notification.event;

import lombok.*;
import org.edu_sharing.kafka.notification.data.NodeDataDTO;
import org.edu_sharing.kafka.notification.data.StatusDTO;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InviteSafeEventDTO extends NodeBaseEventDTO {
    public InviteSafeEventDTO(String id, Date timestamp, String creatorId, String receiverId, StatusDTO status, NodeDataDTO node, String name, String userComment, List<String> permissions) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.name = name;
        this.userComment = userComment;
        this.permissions = permissions;
    }

    private String name;
    private String userComment;
    private List<String> permissions;
}