package org.edu_sharing.messages.data;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class UserInfo {
    @Field("id")
    String id;
    String displayName;
}
