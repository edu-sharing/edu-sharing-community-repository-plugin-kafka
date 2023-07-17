package org.edu_sharing.userData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.notification.event.NotificationEvent;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.edu_sharing.notification.event.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class UserData {
    @MongoId(FieldType.STRING)
    String id;
    String firstName;
    String lastName;
    String email;
    String locale;

    public UserData(String id, String firstName, String lastName, String email, String locale) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.locale = locale;
    }

    private NotificationInterval addToCollectionEventNotificationInterval = NotificationInterval.immediately;
    private NotificationInterval proposaForCollectionEventNotificationInterval = NotificationInterval.immediately;
    private NotificationInterval commentEventNotificationInterval = NotificationInterval.immediately;
    private NotificationInterval inviteEventNotificationInterval = NotificationInterval.immediately;
    private NotificationInterval nodeIssueEventNotificationInterval = NotificationInterval.immediately;
    private NotificationInterval ratingEventNotificationInterval = NotificationInterval.immediately;
    private NotificationInterval workflowEventNotificationInterval = NotificationInterval.immediately;
    private NotificationInterval metadataSuggestionEventNotificationInterval = NotificationInterval.immediately;

    public NotificationInterval getNotificationInterval(NotificationEvent notificationEvent){
        return switch (notificationEvent){
            case AddToCollectionEvent ignored -> addToCollectionEventNotificationInterval;
            case ProposeForCollectionEvent ignored -> proposaForCollectionEventNotificationInterval;
            case CommentEvent ignored -> commentEventNotificationInterval;
            case InviteEvent ignored -> inviteEventNotificationInterval;
            case NodeIssueEvent ignored -> nodeIssueEventNotificationInterval;
            case RatingEvent ignored -> ratingEventNotificationInterval;
            case WorkflowEvent ignored -> workflowEventNotificationInterval;
            case MetadataSuggestionEvent ignored -> workflowEventNotificationInterval;
            default -> throw new IllegalStateException("Unexpected value: " + notificationEvent);
        };
    }
}

