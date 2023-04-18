package org.edu_sharing.kafka.notification.events.data;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserInfo {
    String id;
    String firstName;
    String lastName;
    String email;
    String locale;
}
