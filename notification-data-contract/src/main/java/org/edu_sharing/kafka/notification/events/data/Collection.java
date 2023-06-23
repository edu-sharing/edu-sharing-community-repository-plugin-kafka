package org.edu_sharing.kafka.notification.events.data;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Collection extends NodeData {
}
