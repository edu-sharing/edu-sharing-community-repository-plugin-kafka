package org.edu_sharing.plugin_kafka.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonTypeName("InviteMessage")
public class InviteMessage extends NodeBaseMessage {
    private  String userComment;
    private  String[] permissions;
}