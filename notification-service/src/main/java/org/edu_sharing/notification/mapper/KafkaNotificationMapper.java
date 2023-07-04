package org.edu_sharing.notification.mapper;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.edu_sharing.notification.data.*;
import org.edu_sharing.notification.event.*;
import org.edu_sharing.kafka.notification.data.*;
import org.edu_sharing.kafka.notification.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class KafkaNotificationMapper {

    public static NotificationEvent map(NotificationEventDTO dto) {
        return switch (dto) {
            case AddToCollectionEventDTO addToCollectionEventDTO -> mapAddCollectionEventDTO(addToCollectionEventDTO);
            case CommentEventDTO commentEventDTO -> mapCommentEventDTO(commentEventDTO);
            case InviteEventDTO inviteEventDTO -> mapInviteEventDTO(inviteEventDTO);
            case NodeIssueEventDTO nodeIssueEventDTO -> mapNodeIssueEventDTO(nodeIssueEventDTO);
            case RatingEventDTO ratingEventDTO -> mapRatingEventDTO(ratingEventDTO);
            case WorkflowEventDTO workflowEventDTO -> mapWorkflowEventDTO(workflowEventDTO);
            case MetadataSuggestionEventDTO metadataSuggestionEventDTO ->
                    mapMetadataSuggestionEventDTO(metadataSuggestionEventDTO);
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
    private static Status map(org.edu_sharing.kafka.notification.data.StatusDTO status) {
        if (status == null) {
            return Status.NEW;
        }
        return Status.valueOf(status.toString());
    }

    public static Map<String, Object> copyMapFromDTO(Map<String, Object> map) {
        if(map == null) {
            return null;
        }

        return map.entrySet()
                .stream()
                .map(x -> new ImmutablePair<>(x.getKey()
                        .replace(":", "__")
                        .replace(".", "--"),
                        x.getValue()))
                .collect(Collectors.toMap(Pair::getKey, Map.Entry::getValue));
    }

    private static NodeData map(NodeDataDTO node) {
        if(node == null) {
            return null;
        }

        return new NodeData(
                node.getType(),
                new ArrayList<>(node.getAspects()),
                copyMapFromDTO(node.getProperties())
        );
    }

    private static Collection map(CollectionDTO collection) {
        if(collection == null) {
            return null;
        }

        return new Collection(
                collection.getType(),
                new ArrayList<>(collection.getAspects()),
                copyMapFromDTO(collection.getProperties())
        );
    }

    private static NotificationEvent mapAddCollectionEventDTO(AddToCollectionEventDTO dto) {
        return new AddToCollectionEvent(
                dto.getId(),
                dto.getTimestamp(),
                dto.getCreatorId(),
                dto.getReceiverId(),
                map(dto.getStatus()),
                map(dto.getNode()),
                map(dto.getCollection())
        );
    }

    private static NotificationEvent mapCommentEventDTO(CommentEventDTO dto) {
        return new CommentEvent(
                dto.getId(),
                dto.getTimestamp(),
                dto.getCreatorId(),
                dto.getReceiverId(),
                map(dto.getStatus()),
                map(dto.getNode()),
                dto.getCommentContent(),
                dto.getCommentReference(),
                dto.getEvent()
        );
    }

    private static NotificationEvent mapInviteEventDTO(InviteEventDTO dto) {
        return new InviteEvent(
                dto.getId(),
                dto.getTimestamp(),
                dto.getCreatorId(),
                dto.getReceiverId(),
                map(dto.getStatus()),
                map(dto.getNode()),
                dto.getName(),
                dto.getType(),
                dto.getUserComment(),
                Optional.of(dto)
                        .map(InviteEventDTO::getPermissions)
                        .map(x->x.stream().map(KafkaNotificationMapper::map).collect(Collectors.toList()))
                        .map(ArrayList::new)
                        .orElseGet(ArrayList::new)
        );
    }

    private static Permission map(PermissionDTO permissionDTO) {
        if(permissionDTO == null){
            return null;
        }

        return new Permission(
                permissionDTO.getPermission(),
                permissionDTO.getDe()
        );
    }

    private static NotificationEvent mapNodeIssueEventDTO(NodeIssueEventDTO dto) {
        return new NodeIssueEvent(
                dto.getId(),
                dto.getTimestamp(),
                dto.getCreatorId(),
                dto.getReceiverId(),
                map(dto.getStatus()),
                map(dto.getNode()),
                dto.getEmail(),
                dto.getReason(),
                dto.getUserComment()
        );
    }

    private static NotificationEvent mapRatingEventDTO(RatingEventDTO dto) {
        return new RatingEvent(
                dto.getId(),
                dto.getTimestamp(),
                dto.getCreatorId(),
                dto.getReceiverId(),
                map(dto.getStatus()),
                map(dto.getNode()),
                dto.getNewRating(),
                dto.getRatingSum(),
                dto.getRatingCount()
        );
    }

    private static NotificationEvent mapWorkflowEventDTO(WorkflowEventDTO dto) {
        return new WorkflowEvent(
                dto.getId(),
                dto.getTimestamp(),
                dto.getCreatorId(),
                dto.getReceiverId(),
                map(dto.getStatus()),
                map(dto.getNode()),
                dto.getWorkflowStatus(),
                dto.getUserComment()
        );
    }

    private static NotificationEvent mapMetadataSuggestionEventDTO(MetadataSuggestionEventDTO dto) {
        return new MetadataSuggestionEvent(
                dto.getId(),
                dto.getTimestamp(),
                dto.getCreatorId(),
                dto.getReceiverId(),
                map(dto.getStatus()),
                map(dto.getNode()),
                dto.getCaptionId(),
                dto.getCaption(),
                dto.getParentId(),
                dto.getParentCaption(),
                new WidgetData(
                        dto.getWidget().getId(),
                        dto.getWidget().getCaption())
        );
    }


    // -----------------------------------------------------------------------------------------------------------------


    private static StatusDTO map(Status status) {
        if(status == null) {
            return null;
        }

        return StatusDTO.valueOf(status.toString());
    }

    private static NodeDataDTO map(NodeData node) {
        if(node == null) {
            return null;
        }

        return new NodeDataDTO(
                node.getType(),
                new ArrayList<>(node.getAspects()),
                new HashMap<>(node.getProperties()));
    }

    private static CollectionDTO map(Collection collection) {
        if(collection == null) {
            return null;
        }

        return new CollectionDTO(
                collection.getType(),
                new ArrayList<>(collection.getAspects()),
                new HashMap<>(collection.getProperties()));
    }

    private static WidgetDataDTO map(WidgetData widget) {
        if(widget == null) {
            return null;
        }

        return new WidgetDataDTO(
                widget.getId(),
                widget.getCaption());
    }


    private static NotificationEventDTO mapAddCollectionEvent(AddToCollectionEvent event) {
        return new AddToCollectionEventDTO(
                event.getId(),
                event.getTimestamp(),
                event.getCreatorId(),
                event.getReceiverId(),
                map(event.getStatus()),
                map(event.getNode()),
                map(event.getCollection())
        );
    }


    private static NotificationEventDTO mapCommentEvent(CommentEvent event) {
        return new CommentEventDTO(
                event.getId(),
                event.getTimestamp(),
                event.getCreatorId(),
                event.getReceiverId(),
                map(event.getStatus()),
                map(event.getNode()),
                event.getCommentContent(),
                event.getCommentReference(),
                event.getEvent()
        );
    }

    private static NotificationEventDTO mapInviteEvent(InviteEvent event) {
        return new InviteEventDTO(
                event.getId(),
                event.getTimestamp(),
                event.getCreatorId(),
                event.getReceiverId(),
                map(event.getStatus()),
                map(event.getNode()),
                event.getName(),
                event.getType(),
                event.getUserComment(),
                Optional.of(event)
                        .map(InviteEvent::getPermissions)
                        .map(x->x.stream().map(KafkaNotificationMapper::map).collect(Collectors.toList()))
                        .map(ArrayList::new)
                        .orElseGet(ArrayList::new)
        );
    }

    private static PermissionDTO map(Permission permission) {
        if(permission == null){
            return null;
        }

        return new PermissionDTO(
                permission.getPermission(),
                permission.getDe()
        );
    }

    private static NotificationEventDTO mapNodeIssueEvent(NodeIssueEvent event) {
        return new NodeIssueEventDTO(
                event.getId(),
                event.getTimestamp(),
                event.getCreatorId(),
                event.getReceiverId(),
                map(event.getStatus()),
                map(event.getNode()),
                event.getEmail(),
                event.getReason(),
                event.getUserComment()
        );
    }

    private static NotificationEventDTO mapRatingEvent(RatingEvent event) {
        return new RatingEventDTO(
                event.getId(),
                event.getTimestamp(),
                event.getCreatorId(),
                event.getReceiverId(),
                map(event.getStatus()),
                map(event.getNode()),
                event.getNewRating(),
                event.getRatingSum(),
                event.getRatingCount()
        );
    }

    private static NotificationEventDTO mapWorkflowEvent(WorkflowEvent event) {
        return new WorkflowEventDTO(
                event.getId(),
                event.getTimestamp(),
                event.getCreatorId(),
                event.getReceiverId(),
                map(event.getStatus()),
                map(event.getNode()),
                event.getWorkflowStatus(),
                event.getUserComment()
        );
    }

    private static NotificationEventDTO mapMetadataSuggestionEvent(MetadataSuggestionEvent event) {
        return new MetadataSuggestionEventDTO(
                event.getId(),
                event.getTimestamp(),
                event.getCreatorId(),
                event.getReceiverId(),
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
