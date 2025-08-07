package org.edu_sharing.userData;

import lombok.Value;

@Value
public class UserDataDeletedEvent {
    UserData oldUserData;
}
