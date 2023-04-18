package org.edu_sharing.kafka.notification.events;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.edu_sharing.kafka.notification.events.data.WidgetData;

@Getter
@Jacksonized
@SuperBuilder
public class MetadataSuggestionEventDTO extends NodeBaseEventDTO  {
    private String id;
    private String caption;
    private String parentId;
    private String parentCaption;
    private WidgetData widget;
}
