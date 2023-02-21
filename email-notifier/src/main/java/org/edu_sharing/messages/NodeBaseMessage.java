package org.edu_sharing.messages;

import lombok.Data;

@Data
public abstract class NodeBaseMessage extends BaseMessage {
    private  Node node;
}

