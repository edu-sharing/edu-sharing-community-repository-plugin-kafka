package org.edu_sharing.kafka.notification.events.data;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class WidgetData {
    private String id;
    private String caption;
}
