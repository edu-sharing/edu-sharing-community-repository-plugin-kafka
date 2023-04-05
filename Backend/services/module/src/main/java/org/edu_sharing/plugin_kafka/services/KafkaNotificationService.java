package org.edu_sharing.plugin_kafka.services;

import com.sun.star.lang.IllegalArgumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.kafka.notification.events.*;
import org.edu_sharing.kafka.notification.events.data.Collection;
import org.edu_sharing.kafka.notification.events.data.NodeData;
import org.edu_sharing.kafka.notification.events.data.UserInfo;
import org.edu_sharing.metadataset.v2.MetadataWidget;
import org.edu_sharing.plugin_kafka.config.MailSettings;
import org.edu_sharing.plugin_kafka.kafka.KafkaTemplate;
import org.edu_sharing.plugin_kafka.kafka.SendResult;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.client.tools.I18nAngular;
import org.edu_sharing.repository.server.AuthenticationToolAPI;
import org.edu_sharing.repository.server.tools.Mail;
import org.edu_sharing.repository.server.tools.URLTool;
import org.edu_sharing.repository.server.tools.cache.UserCache;
import org.edu_sharing.repository.server.tools.mailtemplates.MailTemplate;
import org.edu_sharing.restservices.mds.v1.model.MdsValue;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.notification.NotificationService;
import org.edu_sharing.service.notification.Status;
import org.edu_sharing.service.rating.RatingDetails;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KafkaNotificationService implements NotificationService {

    private final KafkaTemplate<String, NotificationEventDTO> kafkaTemplate;
    private final NodeService nodeService;
    private final MailSettings mailSettings;

    public CompletableFuture<SendResult<String, NotificationEventDTO>> send(NotificationEventDTO.NotificationEventDTOBuilder notificationMessageBuilder) {
        notificationMessageBuilder.id(generateMessageId()).timestamp(DateTime.now().toDate());
        NotificationEventDTO notificationMessage = notificationMessageBuilder.build();
        return kafkaTemplate.sendDefault(notificationMessage.getId(), notificationMessage);
    }

    @Override
    public void notifyNodeIssue(String nodeId, String reason, String userEmail, String userComment) throws Throwable {

        HashMap<String, Object> properties = nodeService.getProperties(StoreRef.PROTOCOL_WORKSPACE, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), nodeId);

        try {
            if (StringUtils.isBlank(mailSettings.getAddReplyTo())) {
                throw new IllegalArgumentException("No report receiver is set in the configuration");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("No report receiver is set in the configuration");
        }

        send(NodeIssueEventDTO.builder()
                .creator(UserInfo.builder().email(userEmail).build())
                .receiver(UserInfo.builder().email(mailSettings.getAddReplyTo()).build())
                .reason(reason)
                .userComment(reason)
                .node(NodeData.builder()
                        .property("name", properties.get(CCConstants.CM_NAME).toString())
                        .property("link", URLTool.getNgRenderNodeUrl(nodeId, null, true))
                        .property("link.static", URLTool.getNgRenderNodeUrl(nodeId, null, false))
                        .build()));
    }

    @Override
    public void notifyWorkflowChanged(String nodeId, HashMap<String, Object> nodeProperties, String receiver, String comment, String status) {
        MailTemplate.UserMail sender = MailTemplate.getUserMailData(new AuthenticationToolAPI().getCurrentUser());
        send(WorkflowEventDTO.builder()
                .creator(UserInfo.builder()
                        .displayName(sender.getFullName())
                        .email(sender.getEmail())
                        .build())
                .receiver(UserInfo.builder().email(receiver).build())
                .userComment(comment)
                .workflowStatus(I18nAngular.getTranslationAngular("common", "WORKFLOW." + status))
                .node(NodeData.builder()
                        .properties(nodeProperties)
                        .build()));
    }

    @Override
    public void notifyPersonStatusChanged(String receiver, String firstname, String lastName, String oldStatus, String newStatus) {

    }

    @Override
    public void notifyPermissionChanged(String senderAuthority, String receiverAuthority, String nodeId, String[] permissions, String mailText) throws Throwable {

    }


    @Override
    public void notifyMetadataSetSuggestion(MdsValue mdsValue, MetadataWidget widgetDefinition, List<String> nodes) throws Throwable {

    }

    @Override
    public void notifyComment(String node, String comment, String commentReference, HashMap<String, Object> nodeProperties, Status status) {

        String receiverAuthority = nodeService.getProperty(StoreRef.PROTOCOL_WORKSPACE, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), node, CCConstants.CM_PROP_OWNER);
        MailTemplate.UserMail sender = MailTemplate.getUserMailData(new AuthenticationToolAPI().getCurrentUser());
        MailTemplate.UserMail receiver = MailTemplate.getUserMailData(receiverAuthority);

        send(CommentEventDTO.builder()
                .creator(UserInfo.builder()
                        .displayName(sender.getFullName())
                        .email(sender.getEmail())
                        .build())
                .receiver(UserInfo.builder()
                        .displayName(receiver.getFullName())
                        .email(receiver.getEmail())
                        .build())
                .commentContent(comment)
                .commentReference(commentReference)
                .event(status.toString())
                .node(NodeData.builder()
                        .properties(nodeProperties)
                        .build())
        );
    }

    @Override
    public void notifyCollection(String collectionId, String refNodeId, HashMap<String, Object> collectionProperties, HashMap<String, Object> nodeProperties, Status status) {
        String receiverAuthority = nodeService.getProperty(StoreRef.PROTOCOL_WORKSPACE, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), collectionId, CCConstants.CM_PROP_OWNER);
        MailTemplate.UserMail sender = MailTemplate.getUserMailData(new AuthenticationToolAPI().getCurrentUser());
        MailTemplate.UserMail receiver = MailTemplate.getUserMailData(receiverAuthority);

        send(AddToCollectionEventDTO.builder()
                .creator(UserInfo.builder()
                        .displayName(sender.getFullName())
                        .email(sender.getEmail())
                        .build())
                .receiver(UserInfo.builder()
                        .displayName(receiver.getFullName())
                        .email(receiver.getEmail())
                        .build())
                .collection(Collection.builder()
                        .properties(collectionProperties)
                        .build())
                .node(NodeData.builder()
                        .properties(nodeProperties)
                        .build()));
    }

    @Override
    public void notifyRatingChanged(String nodeId, HashMap<String, Object> nodeProperties, Double rating, RatingDetails accumulatedRatings, Status removed) {
        String receiverAuthority = nodeService.getProperty(StoreRef.PROTOCOL_WORKSPACE, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), nodeId, CCConstants.CM_PROP_OWNER);
        MailTemplate.UserMail receiver = MailTemplate.getUserMailData(receiverAuthority);


        send(RatingEventDTO.builder()
                .creator(UserInfo.builder()
                        .displayName(mailSettings.getFrom())
                        .email(mailSettings.getAddReplyTo())
                        .build())
                .receiver(UserInfo.builder()
                        .displayName(receiver.getFullName())
                        .email(receiver.getEmail())
                        .build())
                .newRating(rating)
                .ratingCount(accumulatedRatings.getOverall().getCount())
                .ratingSum(accumulatedRatings.getOverall().getSum())
                .node(NodeData.builder()
                        .properties(nodeProperties)
                        .build()));
    }

    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
}
