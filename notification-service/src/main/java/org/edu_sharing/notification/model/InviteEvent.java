package org.edu_sharing.notification.model;

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
@TypeAlias("InviteEvent")
@Document(collection = "notification")
public class InviteEvent extends NodeBaseEvent {
    private String name;
    private String type;
    private  String userComment;
    @Singular
    private List<String> permissions;
}