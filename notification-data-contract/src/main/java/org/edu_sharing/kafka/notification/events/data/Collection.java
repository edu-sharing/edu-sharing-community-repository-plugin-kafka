package org.edu_sharing.kafka.notification.events.data;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@SuperBuilder
public class Collection extends NodeData {
}
