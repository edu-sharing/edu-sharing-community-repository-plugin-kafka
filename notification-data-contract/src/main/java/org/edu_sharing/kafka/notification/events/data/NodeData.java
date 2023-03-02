package org.edu_sharing.kafka.notification.events.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.io.Serializable;
import java.util.Map;

@Getter
@Builder
public class NodeData {
    @Singular
    private Map<String, Serializable> properties;
}
