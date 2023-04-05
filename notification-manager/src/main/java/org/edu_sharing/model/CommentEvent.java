package org.edu_sharing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("CommentMessage")
@Document(collation = "notification")
public class CommentEvent extends NodeBaseEvent {
    private  String commentContent;

    /**
     * the id this comment refers to, if any
     */
    private String commentReference;
    private String event;
}