package org.edu_sharing.kafka.notification.events.data;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class WidgetData {
    private String id;
    private String caption;
}
