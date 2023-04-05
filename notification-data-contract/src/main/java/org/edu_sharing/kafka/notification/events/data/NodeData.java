package org.edu_sharing.kafka.notification.events.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Map;

@Getter
@SuperBuilder
public class NodeData {
    @Singular
    private Map<String, Object> properties;
}
