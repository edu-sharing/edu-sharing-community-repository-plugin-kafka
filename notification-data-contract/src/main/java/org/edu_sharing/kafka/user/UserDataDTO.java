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

    private NotificationInterval addToCollectionEvent = NotificationInterval.immediately;
    private NotificationInterval commentEvent = NotificationInterval.immediately;
    private NotificationInterval inviteEvent = NotificationInterval.immediately;
    private NotificationInterval nodeIssueEvent = NotificationInterval.immediately;
    private NotificationInterval ratingEvent = NotificationInterval.immediately;
    private NotificationInterval workflowEvent = NotificationInterval.immediately;
    private NotificationInterval metadataSuggestionEvent = NotificationInterval.immediately;
}


