package org.edu_sharing.kafka.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String locale;

    private NotificationIntervalDTO addToCollectionEvent = NotificationIntervalDTO.immediately;
    private NotificationIntervalDTO proposeForCollectionEvent = NotificationIntervalDTO.immediately;
    private NotificationIntervalDTO commentEvent = NotificationIntervalDTO.immediately;
    private NotificationIntervalDTO inviteEvent = NotificationIntervalDTO.immediately;
    private NotificationIntervalDTO nodeIssueEvent = NotificationIntervalDTO.immediately;
    private NotificationIntervalDTO ratingEvent = NotificationIntervalDTO.immediately;
    private NotificationIntervalDTO workflowEvent = NotificationIntervalDTO.immediately;
    private NotificationIntervalDTO metadataSuggestionEvent = NotificationIntervalDTO.immediately;
}


