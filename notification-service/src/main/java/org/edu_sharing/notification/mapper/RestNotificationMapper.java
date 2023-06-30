package org.edu_sharing.notification.mapper;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.edu_sharing.notification.data.Collection;
import org.edu_sharing.notification.data.NodeData;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.data.WidgetData;
import org.edu_sharing.notification.event.*;
import org.edu_sharing.rest.notification.data.*;
import org.edu_sharing.rest.notification.event.*;
import org.edu_sharing.userData.UserData;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RestNotificationMapper {


    private final Map<String, UserData> userData;

    public org.edu_sharing.rest.notification.event.NotificationEventDTO map(NotificationEvent event) {
        return switch (event) {
            case AddToCollectionEvent addToCollectionEvent -> mapAddCollectionEvent(addToCollectionEvent);
            case CommentEvent commentEvent -> mapCommentEvent(commentEvent);
            case InviteEvent inviteEvent -> mapInviteEvent(inviteEvent);
            case NodeIssueEvent nodeIssueEvent -> mapNodeIssueEvent(nodeIssueEvent);
            case RatingEvent ratingEvent -> mapRatingEvent(ratingEvent);
            case WorkflowEvent workflowEvent -> mapWorkflowEvent(workflowEvent);
            case MetadataSuggestionEvent metadataSuggestionEvent -> mapMetadataSuggestionEvent(metadataSuggestionEvent);
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };
    }

    private StatusDTO map(Status status) {
        return StatusDTO.valueOf(status.toString());
    }

    private UserDataDTO map(UserData userData) {
        return new UserDataDTO(
                userData.getId(),
                userData.getFirstName(),
                userData.getLastName(),
                userData.getEmail());
    }

    private NodeDataDTO map(NodeData node) {
        return new NodeDataDTO(
                node.getType(),
                new ArrayList<>(node.getAspects()),
                copyMapToDTO(node.getProperties()));
    }


    private static CollectionDTO map(Collection collection) {
        return new CollectionDTO(
                collection.getType(),
                new ArrayList<>(collection.getAspects()),
                copyMapToDTO(collection.getProperties()));
    }

    public static Status map(StatusDTO status) {
        return Status.valueOf(status.toString());
    }

    private static WidgetDataDTO map(WidgetData widget) {
        return new WidgetDataDTO(widget.getId(), widget.getCaption());
    }

    public static Map<String, Object> copyMapToDTO(Map<String, Object> map) {
        return map.entrySet()
                .stream()
                .map(x -> new ImmutablePair<>(x.getKey()
                        .replace("__", ":")
                        .replace("--", "."),
                        x.getValue()))
                .collect(Collectors.toMap(Pair::getKey, Map.Entry::getValue));
    }


    private NotificationEventDTO mapAddCollectionEvent(AddToCollectionEvent event) {
        return new AddToCollectionEventDTO(
                event.getId(),
                event.getTimestamp(),
                map(userData.get(event.getCreatorId())),
                map(userData.get(event.getReceiverId())),
                map(event.getStatus()),
                map(event.getNode()),
                map(event.getCollection())
        );
    }


    private NotificationEventDTO mapCommentEvent(CommentEvent event) {
        return new CommentEventDTO(
                event.getId(),
                event.getTimestamp(),
                map(userData.get(event.getCreatorId())),
                map(userData.get(event.getReceiverId())),
                map(event.getStatus()),
                map(event.getNode()),
                event.getCommentContent(),
                event.getCommentReference(),
                event.getEvent()
        );
    }

    private NotificationEventDTO mapInviteEvent(InviteEvent event) {
        return new InviteEventDTO(
                event.getId(),
                event.getTimestamp(),
                map(userData.get(event.getCreatorId())),
                map(userData.get(event.getReceiverId())),
                map(event.getStatus()),
                map(event.getNode()),
                event.getName(),
                event.getType(),
                event.getUserComment(),
                new ArrayList<>(event.getPermissions())
        );
    }

    private NotificationEventDTO mapNodeIssueEvent(NodeIssueEvent event) {
        return new NodeIssueEventDTO(event.getId(),
                event.getTimestamp(),
                new UserDataDTO(null, null, null, event.getEmail()),
                map(userData.get(event.getReceiverId())),
                map(event.getStatus()),
                map(event.getNode()),
                event.getReason(),
                event.getUserComment()
        );
    }

    private NotificationEventDTO mapRatingEvent(RatingEvent event) {
        return new RatingEventDTO(
                event.getId(),
                event.getTimestamp(),
                map(userData.get(event.getCreatorId())),
                map(userData.get(event.getReceiverId())),
                map(event.getStatus()),
                map(event.getNode()),
                event.getNewRating(),
                event.getRatingSum(),
                event.getRatingCount()
        );
    }

    private NotificationEventDTO mapWorkflowEvent(WorkflowEvent event) {
        return new WorkflowEventDTO(
                event.getId(),
                event.getTimestamp(),
                map(userData.get(event.getCreatorId())),
                map(userData.get(event.getReceiverId())),
                map(event.getStatus()),
                map(event.getNode()),
                event.getWorkflowStatus(),
                event.getUserComment()
        );
    }

    private NotificationEventDTO mapMetadataSuggestionEvent(MetadataSuggestionEvent event) {
        return new MetadataSuggestionEventDTO(
                event.getId(),
                event.getTimestamp(),
                map(userData.get(event.getCreatorId())),
                map(userData.get(event.getReceiverId())),
                map(event.getStatus()),
                map(event.getNode()),
                event.getCaptionId(),
                event.getCaption(),
                event.getParentId(),
                event.getParentCaption(),
                map(event.getWidget())
        );
    }

}
