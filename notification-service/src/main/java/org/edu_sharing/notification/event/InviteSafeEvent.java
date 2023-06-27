package org.edu_sharing.notification.event;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("InviteSafeEvent")
@Document(collection = "notification")
public class InviteSafeEvent extends NodeBaseEvent {
    private String name;
    private  String userComment;
    @Singular
    private List<String> permissions;
}