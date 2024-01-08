package org.edu_sharing.notification.mapper;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.edu_sharing.notification.data.*;
import org.edu_sharing.notification.event.*;
import org.edu_sharing.rest.notification.data.*;
import org.edu_sharing.rest.notification.event.*;
import org.edu_sharing.userData.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RestNotificationMapper {


    private final Map<String, UserData> userData;

    public org.edu_sharing.rest.notification.event.NotificationEventDTO map(NotificationEvent event) {
        if (event instanceof AddToCollectionEvent) {
            return mapAddCollectionEvent((AddToCollectionEvent) event);
        } else if (event instanceof ProposeForCollectionEvent) {
            return mapProposeForCollectionEvent((ProposeForCollectionEvent) event);
        } else if (event instanceof CommentEvent) {
            return mapCommentEvent((CommentEvent) event);
        } else if (event instanceof InviteEvent) {
            return mapInviteEvent((InviteEvent) event);
        } else if (event instanceof NodeIssueEvent) {
            return mapNodeIssueEvent((NodeIssueEvent) event);
        } else if (event instanceof RatingEvent) {
            return mapRatingEvent((RatingEvent) event);
        } else if (event instanceof WorkflowEvent) {
            return mapWorkflowEvent((WorkflowEvent) event);
        } else if (event instanceof MetadataSuggestionEvent) {
            return mapMetadataSuggestionEvent((MetadataSuggestionEvent) event);
        } else {
            throw new IllegalStateException("Unexpected value: " + event);
        }
    }

    private StatusDTO map(Status status) {
        if (status == null) {
            return null;
        }

        return StatusDTO.valueOf(status.toString());
    }

    private UserDataDTO map(UserData userData) {
        if (userData == null) {
            return null;
        }

        return new UserDataDTO(
                userData.getId(),
                userData.getFirstName(),
                userData.getLastName(),
                userData.getEmail());
    }

    private NodeDataDTO map(NodeData node) {
        if (node == null) {
            return null;
        }

        return new NodeDataDTO(
                node.getType(),
                new ArrayList<>(node.getAspects()),
                copyMapToDTO(node.getProperties()));
    }


    private static CollectionDTO map(Collection collection) {
        if (collection == null) {
            return null;
        }

        return new CollectionDTO(
                collection.getType(),
                new ArrayList<>(collection.getAspects()),
                copyMapToDTO(collection.getProperties()));
    }

    public static Status map(StatusDTO status) {
        if (status == null) {
            return null;
        }

        return Status.valueOf(status.toString());
    }

    private static WidgetDataDTO map(WidgetData widget) {
        if (widget == null) {
            return null;
        }

        return new WidgetDataDTO(widget.getId(), widget.getCaption());
    }

    public static Map<String, Object> copyMapToDTO(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        return new HashMap<>(map);
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

    private NotificationEventDTO mapProposeForCollectionEvent(ProposeForCollectionEvent event) {
        return new ProposeForCollectionEventDTO(
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
                Optional.of(event)
                        .map(InviteEvent::getPermissions)
                        .map(x -> x.stream().map(Permission::getPermission).collect(Collectors.toList()))
                        .map(ArrayList::new)
                        .orElseGet(ArrayList::new)
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
