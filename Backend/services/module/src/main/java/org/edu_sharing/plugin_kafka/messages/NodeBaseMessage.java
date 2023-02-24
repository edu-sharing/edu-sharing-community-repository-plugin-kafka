package org.edu_sharing.plugin_kafka.messages;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class NodeBaseMessage extends BaseMessage {
    private  Node node;
}

