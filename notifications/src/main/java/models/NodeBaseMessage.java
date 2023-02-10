package models;

import java.io.Serializable;
import java.util.Map;

abstract class NodeBaseMessage extends BaseMessage {
    Node node;

    private static class Node {
        Map<String, Serializable> properties;
    }
}