package org.edu_sharing.plugin_kafka.messages;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Singular;

import java.io.Serializable;
import java.util.Map;

@Getter
@Builder
public class Node {
    @Singular
    private Map<String, Serializable> properties;
}
