package org.edu_sharing.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Notification")
@TypeAlias("InviteMessage")
@JsonTypeName("InviteMessage")
public class InviteMessage extends NodeBaseMessage {
    private  String userComment;
    private  String[] permissions;
}