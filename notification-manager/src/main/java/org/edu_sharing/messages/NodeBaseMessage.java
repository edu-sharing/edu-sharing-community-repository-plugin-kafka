package org.edu_sharing.messages;

import lombok.Data;
import org.edu_sharing.messages.data.Node;

@Data
public abstract class NodeBaseMessage extends BaseMessage {
    private Node node;
}

