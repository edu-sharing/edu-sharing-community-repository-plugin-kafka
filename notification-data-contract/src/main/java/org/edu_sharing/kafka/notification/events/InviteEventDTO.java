package org.edu_sharing.kafka.notification.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Jacksonized
@SuperBuilder
public class InviteEventDTO extends NodeBaseEventDTO {
    private String name;
    private String type;
    private String userComment;
    @Singular
    private List<String> permissions;
}