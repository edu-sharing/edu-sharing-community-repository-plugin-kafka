package org.edu_sharing.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.notification.data.NodeData;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.data.WidgetData;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("MetadataSuggestionEvent")
@Document(collection = "notification")
public class MetadataSuggestionEvent extends NodeBaseEvent {

    public MetadataSuggestionEvent(String id, Date timestamp, String creatorId, String receiverId, Status status, NodeData node, String captionId, String caption, String parentId, String parentCaption, WidgetData widget) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.captionId = captionId;
        this.caption = caption;
        this.parentId = parentId;
        this.parentCaption = parentCaption;
        this.widget = widget;
    }

    private String captionId;
    private String caption;
    private String parentId;
    private String parentCaption;
    private WidgetData widget;
}