package org.edu_sharing.kafka.notification.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonTypeName("InviteMessage")
public class InviteEvent extends NodeBaseEvent {
    private  String userComment;
    private  String[] permissions;
}