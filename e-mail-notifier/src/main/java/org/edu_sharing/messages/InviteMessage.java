package org.edu_sharing.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

@Data
@JsonTypeName("InviteMessage")
public class InviteMessage extends NodeBaseMessage {
    private String userComment;
    private String[] permissions;
}