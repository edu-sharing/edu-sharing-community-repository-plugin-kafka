package org.edu_sharing.kafka.notification.events.data;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfo {
    String id;
    String displayName;
    String email;
}
