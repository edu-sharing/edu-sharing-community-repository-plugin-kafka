package org.edu_sharing.kafka.notification.event;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Jacksonized
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InviteEventDTO extends NodeBaseEventDTO {
    private String name;
    private String type;
    private String userComment;
    @Singular
    private List<String> permissions;
}