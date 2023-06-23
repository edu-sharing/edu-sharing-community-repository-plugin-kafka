package org.edu_sharing.kafka.notification.events;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.edu_sharing.kafka.notification.events.data.WidgetData;

@Data
@Jacksonized
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MetadataSuggestionEventDTO extends NodeBaseEventDTO  {
    private String captionId;
    private String caption;
    private String parentId;
    private String parentCaption;
    private WidgetData widget;
}
