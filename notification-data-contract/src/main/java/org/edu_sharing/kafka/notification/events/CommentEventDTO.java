package org.edu_sharing.kafka.notification.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonTypeName("CommentMessage")
public class CommentEventDTO extends NodeBaseEventDTO {
    private  String commentContent;

    /**
     * the id this comment refers to, if any
     */
    private String commentReference;
    private String event;
}