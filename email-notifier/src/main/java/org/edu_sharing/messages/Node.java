package org.edu_sharing.messages;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class Node {
    private Map<String, Serializable> properties;
}
