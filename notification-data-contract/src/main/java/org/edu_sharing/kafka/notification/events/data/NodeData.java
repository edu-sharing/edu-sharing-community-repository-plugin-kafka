package org.edu_sharing.kafka.notification.events.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Jacksonized
@SuperBuilder
public class NodeData {
    @Singular
    private Map<String, Object> properties;
}
