package org.edu_sharing.notification.mapper;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.edu_sharing.notification.data.Collection;
import org.edu_sharing.notification.data.NodeData;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.data.WidgetData;
import org.edu_sharing.notification.event.*;
import org.edu_sharing.kafka.notification.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public  class KafkaNotificationMapper {

    public static NotificationEvent map(NotificationEventDTO dto) {
        return switch (dto) {
            case AddToCollectionEventDTO addToCollectionEventDTO -> mapAddCollectionEventDTO(addToCollectionEventDTO);
            case CommentEventDTO commentEventDTO -> mapCommentEventDTO(commentEventDTO);
            case InviteEventDTO inviteEventDTO -> mapInviteEventDTO(inviteEventDTO);
            case NodeIssueEventDTO nodeIssueEventDTO -> mapNodeIssueEventDTO(nodeIssueEventDTO);
            case RatingEventDTO ratingEventDTO -> mapRatingEventDTO(ratingEventDTO);
            case WorkflowEventDTO workflowEventDTO -> mapWorkflowEventDTO(workflowEventDTO);
            case MetadataSuggestionEventDTO metadataSuggestionEventDTO -> mapMetadataSuggestionEventDTO(metadataSuggestionEventDTO);
            default -> throw new IllegalStateException("Unexpected value: " + dto);
        };
    }

    public static NotificationEventDTO map(NotificationEvent event) {
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



    private static void mapNotificationEventDTO(NotificationEvent event, NotificationEventDTO dto) {
        event.setId(dto.getId());
        event.setTimestamp(dto.getTimestamp());
        event.setCreatorId(dto.getCreatorId());
        event.setReceiverId(dto.getReceiverId());
        event.setStatus(map(dto.getStatus()));
    }

    private static Status map(org.edu_sharing.kafka.notification.data.Status status) {
        if(status == null){
            return Status.NEW;
        }
        return Status.valueOf(status.toString());
    }

    public static Map<String, Object> copyMapFromDTO(Map<String, Object> map){
        return map.entrySet()
                .stream()
                .map(x -> new ImmutablePair<>(x.getKey()
                        .replace(":","__")
                        .replace(".","--"),
                        x.getValue()))
                .collect(Collectors.toMap(Pair::getKey, Map.Entry::getValue));
    }

    private static void mapNodeBaseEventDTO(NodeBaseEvent event, NodeBaseEventDTO dto) {
        mapNotificationEventDTO(event, dto);

        event.setNode(NodeData.builder().properties(copyMapFromDTO(dto.getNode().getProperties())).build());
    }


    private static NotificationEvent mapAddCollectionEventDTO(AddToCollectionEventDTO dto) {
        AddToCollectionEvent event = new AddToCollectionEvent();
        mapNodeBaseEventDTO(event, dto);

        event.setCollection(Collection.builder().properties(copyMapFromDTO(dto.getCollection().getProperties())).build());

        return event;
    }

    private static NotificationEvent mapCommentEventDTO(CommentEventDTO dto) {
        CommentEvent event = new CommentEvent();
        mapNodeBaseEventDTO(event, dto);

        event.setCommentContent(dto.getCommentContent());
        event.setCommentReference(dto.getCommentReference());
        event.setEvent(dto.getEvent());

        return event;
    }

    private static NotificationEvent mapInviteEventDTO(InviteEventDTO dto) {
        InviteEvent event = new InviteEvent();
        mapNodeBaseEventDTO(event, dto);

        event.setName(dto.getName());
        event.setType(dto.getType());
        event.setUserComment(dto.getUserComment());
        event.setPermissions(new ArrayList<>(dto.getPermissions()));

        return event;
    }

    private static NotificationEvent mapNodeIssueEventDTO(NodeIssueEventDTO dto) {
        NodeIssueEvent event = new NodeIssueEvent();
        mapNodeBaseEventDTO(event, dto);

        event.setEmail(dto.getEmail());
        event.setReason(dto.getReason());
        event.setUserComment(dto.getUserComment());

        return event;
    }

    private static NotificationEvent mapRatingEventDTO(RatingEventDTO dto) {
        RatingEvent event = new RatingEvent();
        mapNodeBaseEventDTO(event, dto);

        event.setNewRating(dto.getNewRating());
        event.setRatingSum(dto.getRatingSum());
        event.setRatingCount(dto.getRatingCount());

        return event;
    }

    private static NotificationEvent mapWorkflowEventDTO(WorkflowEventDTO dto) {
        WorkflowEvent event = new WorkflowEvent();
        mapNodeBaseEventDTO(event, dto);

        event.setWorkflowStatus(dto.getWorkflowStatus());
        event.setUserComment(dto.getUserComment());

        return event;
    }

    private static NotificationEvent mapMetadataSuggestionEventDTO(MetadataSuggestionEventDTO dto) {
        MetadataSuggestionEvent event = new MetadataSuggestionEvent();
        mapNodeBaseEventDTO(event, dto);

        event.setCaptionId(dto.getCaptionId());
        event.setCaption(dto.getCaption());
        event.setParentId(dto.getParentId());
        event.setParentCaption(dto.getParentCaption());
        event.setWidget(WidgetData.builder()
                .id(dto.getWidget().getId())
                .caption(dto.getWidget().getCaption())
                .build());

        return event;
    }



    // -----------------------------------------------------------------------------------------------------------------



    private static void mapNotificationEvent(NotificationEvent event, NotificationEventDTO dto) {
        dto.setId(event.getId());
        dto.setTimestamp(event.getTimestamp());
        dto.setCreatorId(event.getCreatorId());
        dto.setReceiverId(event.getReceiverId());
        dto.setStatus(map(event.getStatus()));
    }

    private static org.edu_sharing.kafka.notification.data.Status map(Status status) {
        return org.edu_sharing.kafka.notification.data.Status.valueOf(status.toString());
    }

    private static void mapNodeBaseEvent(NodeBaseEvent event, NodeBaseEventDTO dto) {
        mapNotificationEvent(event, dto);

        dto.setNode(org.edu_sharing.kafka.notification.data.NodeData.builder().properties(new HashMap<>(event.getNode().getProperties())).build());
    }


    private static NotificationEventDTO mapAddCollectionEvent(AddToCollectionEvent event) {
        AddToCollectionEventDTO dto = new AddToCollectionEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setCollection(org.edu_sharing.kafka.notification.data.Collection.builder().properties(new HashMap<>(event.getCollection().getProperties())).build());

        return dto;
    }

    private static NotificationEventDTO mapCommentEvent(CommentEvent event) {
        CommentEventDTO dto = new CommentEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setCommentContent(event.getCommentContent());
        dto.setCommentReference(event.getCommentReference());
        dto.setEvent(event.getEvent());

        return dto;
    }

    private static NotificationEventDTO mapInviteEvent(InviteEvent event) {
        InviteEventDTO dto = new InviteEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setName(event.getName());
        dto.setType(event.getType());
        dto.setUserComment(event.getUserComment());
        dto.setPermissions(new ArrayList<>(event.getPermissions()));

        return dto;
    }

    private static NotificationEventDTO mapNodeIssueEvent(NodeIssueEvent event) {
        NodeIssueEventDTO dto = new NodeIssueEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setEmail(event.getEmail());
        dto.setReason(event.getReason());
        dto.setUserComment(event.getUserComment());

        return dto;
    }

    private static NotificationEventDTO mapRatingEvent(RatingEvent event) {
        RatingEventDTO dto = new RatingEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setNewRating(event.getNewRating());
        dto.setRatingSum(event.getRatingSum());
        dto.setRatingCount(event.getRatingCount());

        return dto;
    }

    private static NotificationEventDTO mapWorkflowEvent(WorkflowEvent event) {
        WorkflowEventDTO dto = new WorkflowEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setWorkflowStatus(event.getWorkflowStatus());
        dto.setUserComment(event.getUserComment());

        return dto;
    }

    private static NotificationEventDTO mapMetadataSuggestionEvent(MetadataSuggestionEvent event) {
        MetadataSuggestionEventDTO dto = new MetadataSuggestionEventDTO();
        mapNodeBaseEvent(event, dto);

        dto.setCaptionId(event.getCaptionId());
        dto.setCaption(event.getCaption());
        dto.setParentId(event.getParentId());
        dto.setParentCaption(event.getParentCaption());
        dto.setWidget(org.edu_sharing.kafka.notification.data.WidgetData.builder()
                .id(event.getWidget().getId())
                .caption(event.getWidget().getCaption())
                .build());

        return dto;
    }

}
