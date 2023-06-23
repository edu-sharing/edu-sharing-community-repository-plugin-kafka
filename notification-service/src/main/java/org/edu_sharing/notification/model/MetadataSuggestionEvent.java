package org.edu_sharing.notification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.kafka.notification.events.data.WidgetData;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("MetadataSuggestionEvent")
@Document(collection = "notification")
public class MetadataSuggestionEvent extends NodeBaseEvent {
    private String captionId;
    private String caption;
    private String parentId;
    private String parentCaption;
    private WidgetData widget;
}