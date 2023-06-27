package org.edu_sharing.notification.mapper;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.event.*;
import org.edu_sharing.rest.notification.data.Collection;
import org.edu_sharing.rest.notification.data.NodeData;
import org.edu_sharing.rest.notification.data.WidgetData;
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

    private void mapNotificationEvent(NotificationEvent event, NotificationEventDTO dto) {
        dto.setId(event.getId());
        dto.setTimestamp(event.getTimestamp());
        dto.setCreator(map(userData.get(event.getCreatorId())));
        dto.setReceiver(map(userData.get(event.getReceiverId())));
        dto.setStatus(map(event.getStatus()));
    }

    private org.edu_sharing.rest.notification.data.Status map(Status status) {
        return org.edu_sharing.rest.notification.data.Status.valueOf(status.toString());
    }

    private org.edu_sharing.rest.notification.data.UserData map(UserData userData) {
        return new org.edu_sharing.rest.notification.data.UserData(
                userData.getFirstName(),
                userData.getLastName(),
                userData.getEmail());
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

    private void mapNodeBaseEvent(NodeBaseEvent event, NodeBaseEventDTO dto) {
        mapNotificationEvent(event, dto);

        dto.setNode(new NodeData(copyMapToDTO(event.getNode().getProperties())));
    }


    private NotificationEventDTO mapAddCollectionEvent(AddToCollectionEvent event) {
        AddToCollectionEventDTO dto = new AddToCollectionEventDTO();
        mapNodeBaseEvent(event, dto);

        Collection collection = new Collection();
        collection.setProperties(copyMapToDTO(event.getCollection().getProperties()));
        dto.setCollection(collection);

        return dto;
    }

    private NotificationEventDTO mapCommentEvent(CommentEvent event) {
        CommentEventDTO dto = new CommentEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setCommentContent(event.getCommentContent());
        dto.setCommentReference(event.getCommentReference());
        dto.setEvent(event.getEvent());

        return dto;
    }

    private NotificationEventDTO mapInviteEvent(InviteEvent event) {
        InviteEventDTO dto = new InviteEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setName(event.getName());
        dto.setType(event.getType());
        dto.setUserComment(event.getUserComment());
        dto.setPermissions(new ArrayList<>(event.getPermissions()));

        return dto;
    }

    private NotificationEventDTO mapNodeIssueEvent(NodeIssueEvent event) {
        NodeIssueEventDTO dto = new NodeIssueEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setCreator(new org.edu_sharing.rest.notification.data.UserData(null, null, event.getEmail()));
        dto.setReason(event.getReason());
        dto.setUserComment(event.getUserComment());

        return dto;
    }

    private NotificationEventDTO mapRatingEvent(RatingEvent event) {
        RatingEventDTO dto = new RatingEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setNewRating(event.getNewRating());
        dto.setRatingSum(event.getRatingSum());
        dto.setRatingCount(event.getRatingCount());

        return dto;
    }

    private NotificationEventDTO mapWorkflowEvent(WorkflowEvent event) {
        WorkflowEventDTO dto = new WorkflowEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setWorkflowStatus(event.getWorkflowStatus());
        dto.setUserComment(event.getUserComment());

        return dto;
    }

    private NotificationEventDTO mapMetadataSuggestionEvent(MetadataSuggestionEvent event) {
        MetadataSuggestionEventDTO dto = new MetadataSuggestionEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setCaptionId(event.getCaptionId());
        dto.setCaption(event.getCaption());
        dto.setParentId(event.getParentId());
        dto.setParentCaption(event.getParentCaption());
        dto.setWidget(new WidgetData(event.getWidget().getId(), event.getWidget().getCaption()));

        return dto;
    }

    public static Status map(org.edu_sharing.rest.notification.data.Status status) {
        return Status.valueOf(status.toString());
    }
}
