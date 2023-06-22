package org.edu_sharing.kafka.notification.events;

import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Jacksonized
@SuperBuilder
public class InviteSafeEventDTO extends NodeBaseEventDTO {
    private String name;
    private String userComment;
    @Singular
    private List<String> permissions;
}