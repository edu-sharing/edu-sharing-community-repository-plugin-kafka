package org.edu_sharing.plugin_kafka.services;

import com.sun.star.lang.IllegalArgumentException;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.alfresco.workspace_administration.NodeServiceInterceptor;
import org.edu_sharing.kafka.notification.events.*;
import org.edu_sharing.kafka.notification.events.data.Collection;
import org.edu_sharing.kafka.notification.events.data.NodeData;
import org.edu_sharing.kafka.notification.events.data.UserInfo;
import org.edu_sharing.kafka.notification.events.data.WidgetData;
import org.edu_sharing.metadataset.v2.MetadataWidget;
import org.edu_sharing.plugin_kafka.config.MailSettings;
import org.edu_sharing.plugin_kafka.config.Report;
import org.edu_sharing.plugin_kafka.kafka.KafkaTemplate;
import org.edu_sharing.plugin_kafka.kafka.SendResult;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.client.tools.I18nAngular;
import org.edu_sharing.repository.server.AuthenticationToolAPI;
import org.edu_sharing.repository.server.tools.I18nServer;
import org.edu_sharing.repository.server.tools.URLTool;
import org.edu_sharing.repository.server.tools.mailtemplates.MailTemplate;
import org.edu_sharing.restservices.mds.v1.model.MdsValue;
import org.edu_sharing.service.notification.NotificationConfig;
import org.edu_sharing.service.notification.NotificationService;
import org.edu_sharing.service.notification.Status;
import org.edu_sharing.service.rating.RatingDetails;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service("kafkaNotificationService")
@NoArgsConstructor  // Required for proxying by CGLib (CGLib is used because we don't use an interface here...)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KafkaNotificationService implements NotificationService {


    @NonNull
    private KafkaTemplate<String, NotificationEventDTO> kafkaTemplate;
    @NonNull
    private MailSettings mailSettings;

    public CompletableFuture<SendResult<String, NotificationEventDTO>> send(NotificationEventDTO.NotificationEventDTOBuilder<?, ?> notificationMessageBuilder) {
        notificationMessageBuilder.id(generateMessageId()).timestamp(DateTime.now().toDate());
        NotificationEventDTO notificationMessage = notificationMessageBuilder.build();
        return kafkaTemplate.sendDefault(notificationMessage.getId(), notificationMessage);
    }

    @Override
    public void notifyNodeIssue(String nodeId, String reason, Map<String, Object> nodeProperties, String userEmail, String userComment) throws Throwable {

        if (Optional.of(mailSettings).map(MailSettings::getReport).map(Report::getReceiver).map(StringUtils::isBlank).orElse(true)) {
            throw new IllegalArgumentException("No report receiver is set in the configuration");
        }

        send(NodeIssueEventDTO.builder()
                .creator(UserInfo.builder()
                        .email(userEmail)
                        .locale(new AuthenticationToolAPI().getCurrentLocale().replace("_", "-")) // TODO
                        .build())
                .receiver(UserInfo.builder()
                        .email(mailSettings.getReport().getReceiver())
                        .locale(new AuthenticationToolAPI().getCurrentLocale().replace("_", "-")) // TODO
                        .build())
                .reason(reason)
                .userComment(reason)
                .node(createNodeData(nodeId, getSimplifiedNodeProperties(nodeProperties))));
    }

    @Override
    public void notifyWorkflowChanged(String nodeId, Map<String, Object> nodeProperties, String receiver, String comment, String status) {
        MailTemplate.UserMail sender = MailTemplate.getUserMailData(new AuthenticationToolAPI().getCurrentUser());
        send(WorkflowEventDTO.builder()
                .creator(UserInfo.builder()
                        .firstName(sender.getFirstName())
                        .lastName(sender.getLastName())
                        .email(sender.getEmail())
                        .locale(new AuthenticationToolAPI().getCurrentLocale()) // TODO
                        .build())
                .receiver(UserInfo.builder().email(receiver).build())
                .userComment(comment)
                .workflowStatus(I18nAngular.getTranslationAngular("common", "WORKFLOW." + status))
                .node(createNodeData(nodeId, getSimplifiedNodeProperties(nodeProperties))));
    }

    @Override
    public void notifyPersonStatusChanged(String receiver, String firstname, String lastName, String oldStatus, String newStatus) {
        //TODO
        Map<String, String> replace = new HashMap<>();
        replace.put("firstName", firstname);
        replace.put("lastName", lastName);
        replace.put("oldStatus", I18nAngular.getTranslationAngular("permissions", "PERMISSIONS.USER_STATUS." + oldStatus));
        replace.put("newStatus", I18nAngular.getTranslationAngular("permissions", "PERMISSIONS.USER_STATUS." + newStatus));
        try {
            String template = "userStatusChanged";
            MailTemplate.sendMail(receiver, template, replace);
        } catch (Exception e) {
            log.warn("Can not send status notify mail to user: " + e.getMessage(), e);
        }
    }

    @Override
    public void notifyPermissionChanged(String senderAuthority, String receiverAuthority, String nodeId, Map<String, Object> nodeProperties, String[] aspects, String[] permissions, String mailText) throws Throwable {


        // if the receiver is the creator itself, skip it (because it is automatically added)
        String nodeCreator = (String) nodeProperties.get(CCConstants.CM_PROP_C_CREATOR);
        if (receiverAuthority.equals(nodeCreator)) {
            return;
        }

        MailTemplate.UserMail sender = MailTemplate.getUserMailData(senderAuthority);
        MailTemplate.UserMail receiver = MailTemplate.getUserMailData(receiverAuthority);

        String nodeType = (String) nodeProperties.get(CCConstants.NODETYPE);
        String invitationType = "invited";
        if (CCConstants.CCM_VALUE_SCOPE_SAFE.equals(NodeServiceInterceptor.getEduSharingScope())) {
            invitationType = "invited_safe";
        } else if (nodeType.equals(CCConstants.CCM_TYPE_MAP) && Arrays.asList(aspects).contains(CCConstants.CCM_ASPECT_COLLECTION)) {
            invitationType = "invited_collection";
        }

        String name = nodeType.equals(CCConstants.CCM_TYPE_IO)
                ? (String) nodeProperties.get(CCConstants.LOM_PROP_GENERAL_TITLE)
                : (String) nodeProperties.get(CCConstants.CM_PROP_C_TITLE);

        if (StringUtils.isBlank(name)) {
            name = (String) nodeProperties.get(CCConstants.CM_NAME);
        }

        List<String> permissionList = Arrays.stream(permissions)
                .filter(perm -> !(CCConstants.CCM_VALUE_SCOPE_SAFE.equals(NodeServiceInterceptor.getEduSharingScope()) && Objects.equals(CCConstants.PERMISSION_CC_PUBLISH, perm)))
                .map(perm -> I18nServer.getTranslationDefaultResourcebundle(I18nServer.getPermissionDescription(perm), new AuthenticationToolAPI().getCurrentLocale()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        send(InviteEventDTO.builder()
                .creator(UserInfo.builder()
                        .firstName(sender.getFirstName())
                        .lastName(sender.getLastName())
                        .email(sender.getEmail())
                        .locale(new AuthenticationToolAPI().getCurrentLocale()) // TODO
                        .build())
                .receiver(UserInfo.builder()
                        .firstName(receiver.getFirstName())
                        .lastName(receiver.getLastName())
                        .email(receiver.getEmail())
                        .locale(new AuthenticationToolAPI().getCurrentLocale()) // TODO
                        .build())
                .name(name)
                .type(invitationType)
                .userComment(mailText)
                .permissions(permissionList)
                .node(createNodeData(nodeId, nodeProperties)));
    }


    @Override
    public void notifyMetadataSetSuggestion(MdsValue mdsValue, MetadataWidget widgetDefinition, List<String> nodes, List<Map<String, Object>> nodePropertiesList) throws Throwable {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        UserInfo.UserInfoBuilder senderUserInfo = UserInfo.builder().locale(new AuthenticationToolAPI().getCurrentLocale());// TODO
        if (currentUser != null) {
            MailTemplate.UserMail sender = MailTemplate.getUserMailData(currentUser);
            senderUserInfo.firstName(sender.getFirstName())
                    .lastName(sender.getLastName())
                    .lastName(sender.getLastName())
                    .email(sender.getEmail());
        }


        String[] receivers = widgetDefinition.getSuggestionReceiver().split(",");
        for (String receiverAuthority : receivers) {
            MailTemplate.UserMail receiver = MailTemplate.getUserMailData(receiverAuthority);
            MetadataSuggestionEventDTO.MetadataSuggestionEventDTOBuilder<?,?> builder = MetadataSuggestionEventDTO.builder()
                    .creator(senderUserInfo.build())
                    .receiver(UserInfo.builder()
                            .firstName(receiver.getFirstName())
                            .lastName(receiver.getLastName())
                            .email(receiver.getEmail())
                            .locale(new AuthenticationToolAPI().getCurrentLocale()) // TODO
                            .build())
                    .widget(WidgetData.builder()
                            .id(widgetDefinition.getId())
                            .caption(widgetDefinition.getCaption())
                            .build())
                    .id(mdsValue.getId())
                    .caption(mdsValue.getCaption())
                    .parentId(mdsValue.getParent())
                    .parentCaption(mdsValue.getParent() == null ? null : widgetDefinition.getValuesAsMap().get(mdsValue.getParent()).getCaption());

            if(nodes.size() == 0){
                send(builder);
            }

            for (int i = 0; i < nodes.size(); i++) {
                send(builder.node(createNodeData(nodes.get(i), nodePropertiesList.get(i)))
                );
            }

        }
    }

    @Override
    public void notifyComment(String node, String comment, String commentReference, Map<String, Object> nodeProperties, Status status) {
        String receiverAuthority = (String) nodeProperties.get(CCConstants.CM_PROP_C_CREATOR);
        nodeProperties = getSimplifiedNodeProperties(nodeProperties);

        MailTemplate.UserMail sender = MailTemplate.getUserMailData(new AuthenticationToolAPI().getCurrentUser());
        MailTemplate.UserMail receiver = MailTemplate.getUserMailData(receiverAuthority);

        send(CommentEventDTO.builder()
                .creator(UserInfo.builder()
                        .firstName(sender.getFirstName())
                        .lastName(sender.getLastName())
                        .email(sender.getEmail())
                        .locale(new AuthenticationToolAPI().getCurrentLocale().replace("_", "-")) // TODO
                        .build())
                .receiver(UserInfo.builder()
                        .firstName(receiver.getFirstName())
                        .lastName(receiver.getLastName())
                        .email(receiver.getEmail())
                        .locale(new AuthenticationToolAPI().getCurrentLocale().replace("_", "-")) // TODO
                        .build())
                .commentContent(comment)
                .commentReference(commentReference)
                .event(status.toString())
                .node(createNodeData(node, nodeProperties))
        );
    }

    @Override
    public void notifyCollection(String collectionId, String refNodeId, Map<String, Object> collectionProperties, Map<String, Object> nodeProperties, Status status) {

        String receiverAuthority = (String) collectionProperties.get(CCConstants.CM_PROP_C_CREATOR);
        MailTemplate.UserMail sender = MailTemplate.getUserMailData(new AuthenticationToolAPI().getCurrentUser());
        MailTemplate.UserMail receiver = MailTemplate.getUserMailData(receiverAuthority);

        send(AddToCollectionEventDTO.builder()
                .creator(UserInfo.builder()
                        .firstName(sender.getFirstName())
                        .lastName(sender.getLastName())
                        .email(sender.getEmail())
                        .locale(new AuthenticationToolAPI().getCurrentLocale().replace("_", "-")) // TODO
                        .build())
                .receiver(UserInfo.builder()
                        .firstName(receiver.getFirstName())
                        .lastName(receiver.getLastName())
                        .email(receiver.getEmail())
                        .locale(new AuthenticationToolAPI().getCurrentLocale().replace("_", "-")) // TODO
                        .build())
                .collection(Collection.builder()
                        .properties(getSimplifiedNodeProperties(collectionProperties))
                        .property("link", URLTool.getNgRenderNodeUrl(collectionId, null, true))
                        .property("link.static", URLTool.getNgRenderNodeUrl(collectionId, null, false))
                        .build())
                .node(createNodeData(refNodeId, getSimplifiedNodeProperties(nodeProperties))));
    }


    @Override
    public void notifyRatingChanged(String nodeId, Map<String, Object> nodeProperties, Double rating, RatingDetails accumulatedRatings, Status removed) {
        String receiverAuthority = (String) nodeProperties.get(CCConstants.CM_PROP_C_CREATOR);
        MailTemplate.UserMail receiver = MailTemplate.getUserMailData(receiverAuthority);

        if (Optional.of(mailSettings).map(MailSettings::getReport).map(Report::getReceiver).map(StringUtils::isBlank).orElse(true)) {
            log.warn("notifyRatingChanged: No report receiver is set in the configuration");
            return;
        }

        send(RatingEventDTO.builder()
                .creator(UserInfo.builder()
                        .lastName(mailSettings.getFrom())
                        .email(mailSettings.getReport().getReceiver())
                        .locale(new AuthenticationToolAPI().getCurrentLocale().replace("_", "-")) // TODO
                        .build())
                .receiver(UserInfo.builder()
                        .firstName(receiver.getFirstName())
                        .lastName(receiver.getLastName())
                        .email(receiver.getEmail())
                        .locale(new AuthenticationToolAPI().getCurrentLocale().replace("_", "-")) // TODO
                        .build())
                .newRating(rating)
                .ratingCount(accumulatedRatings.getOverall().getCount())
                .ratingSum(accumulatedRatings.getOverall().getSum())
                .node(createNodeData(nodeId, getSimplifiedNodeProperties(nodeProperties))));
    }


    private static NodeData createNodeData(String nodeId, Map<String, Object> nodeProperties) {
        return NodeData.builder()
                .properties(nodeProperties)
                .property("link", URLTool.getNgRenderNodeUrl(nodeId, null, true))
                .property("link.static", URLTool.getNgRenderNodeUrl(nodeId, null, false))
                .build();
    }

    private static Map<String, Object> getSimplifiedNodeProperties(Map<String, Object> nodeProperties) {
        // Thymeleaf can't handle : in field name
        return nodeProperties.entrySet().stream().collect(Collectors.toMap(x -> CCConstants.getValidLocalName(x.getKey()).replace(":","_"), Map.Entry::getValue));
    }


    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void setConfig(NotificationConfig config) throws Exception {
        NotificationService.super.setConfig(config);
        // @TODO: send config to kafka
    }
}
