package org.edu_sharing.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

@Data
@Document(collection = "Notification")
@TypeAlias("CommentMessage")
@JsonTypeName("CommentMessage")
public class CommentMessage extends NodeBaseMessage {
    private  String commentContent;

    /**
     * the id this comment refers to, if any
     */
    @Nullable private String commentReference;
}