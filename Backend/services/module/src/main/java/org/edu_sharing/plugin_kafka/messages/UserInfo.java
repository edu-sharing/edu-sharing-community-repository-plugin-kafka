package org.edu_sharing.plugin_kafka.messages;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfo {
    String id;
    String displayName;
    String email;
}
