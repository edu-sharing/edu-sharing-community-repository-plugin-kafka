package org.edu_sharing.plugin_kafka.services;

import com.sun.star.lang.IllegalArgumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.kafka.notification.events.NodeIssueEventDTO;
import org.edu_sharing.kafka.notification.events.NotificationEventDTO;
import org.edu_sharing.kafka.notification.events.data.NodeData;
import org.edu_sharing.kafka.notification.events.data.UserInfo;
import org.edu_sharing.metadataset.v2.MetadataWidget;
import org.edu_sharing.plugin_kafka.kafka.KafkaTemplate;
import org.edu_sharing.plugin_kafka.kafka.SendResult;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.tools.Mail;
import org.edu_sharing.repository.server.tools.URLTool;
import org.edu_sharing.restservices.mds.v1.model.MdsValue;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.notification.NotificationService;
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

    public CompletableFuture<SendResult<String, NotificationEventDTO>> send(NotificationEventDTO notificationMessage) {
        return kafkaTemplate.sendDefault(notificationMessage.getId(), notificationMessage);
    }

    @Override
    public void notifyNodeIssue(String nodeId, String reason, String userEmail, String userComment) throws Throwable {

        HashMap<String, Object> properties = nodeService.getProperties(StoreRef.PROTOCOL_WORKSPACE, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), nodeId);

        String receiver;
        try {
            Mail mail = new Mail();
            receiver = mail.getConfig().getString("report.receiver");
            if (StringUtils.isBlank(receiver)) {
                throw new IllegalArgumentException("No report receiver is set in the configuration");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("No report receiver is set in the configuration");
        }

        NodeIssueEventDTO event = NodeIssueEventDTO.builder()
                .id(generateMessageId())
                .timestamp(DateTime.now().toDate())
                .creator(UserInfo.builder()
                        .email(userEmail)
                        .build())
                .receiver(UserInfo.builder()
                        .email(receiver)
                        .build())
                .reason(reason)
                .userComment(reason)
                .node(NodeData.builder()
                        .property("name", properties.get(CCConstants.CM_NAME).toString())
                        .property("link", URLTool.getNgRenderNodeUrl(nodeId, null, true))
                        .property("link.static", URLTool.getNgRenderNodeUrl(nodeId, null, false))
                        .build())
                .build();

        send(event);
    }

    @Override
    public void notifyWorkflowChanged(String nodeId, HashMap<String, Object> nodeProperties, String receiver, String comment, String status) {

    }

    @Override
    public void notifyPersonStatusChanged(String receiver, String firstname, String lastName, String oldStatus, String newStatus) {

    }

    @Override
    public void notifyPermissionChanged(String senderAuthority, String receiverAuthority, String nodeId, String[] permissions, String mailText) throws Throwable {

    }

    @Override
    public void notifyGroupSignupList(String groupEmail, String groupName, NodeRef userRef) throws Exception {

    }

    @Override
    public void notifyGroupSignupUser(String userEmail, String groupName, NodeRef userRef) throws Exception {

    }

    @Override
    public void notifyGroupSignupAdmin(String groupEmail, String groupName, NodeRef userRef) throws Exception {

    }

    @Override
    public void notifyGroupSignupHandeld(NodeRef userRef, String groupName, boolean add) throws Exception {

    }

    @Override
    public void notifyMetadataSetSuggestion(MdsValue mdsValue, MetadataWidget widgetDefinition, List<String> nodes) throws Throwable {

    }

    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
}
