package org.edu_sharing.plugin_kafka.services;

import com.sun.star.lang.IllegalArgumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.plugin_kafka.kafka.KafkaTemplate;
import org.edu_sharing.plugin_kafka.config.KafkaSettings;
import org.edu_sharing.plugin_kafka.kafka.SendResult;
import org.edu_sharing.plugin_kafka.messages.*;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.tools.Mail;
import org.edu_sharing.repository.server.tools.URLTool;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.notification.NotificationService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KafkaNotificationService implements NotificationService {

    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;
    private final NodeService nodeService;

    public CompletableFuture<SendResult<String, BaseMessage>> send(BaseMessage baseMessage) {
        return kafkaTemplate.sendDefault(baseMessage.getId(), baseMessage);
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

        NodeIssueMessage message = NodeIssueMessage.builder()
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
                .node(Node.builder()
                        .property("name", properties.get(CCConstants.CM_NAME).toString())
                        .property("link", URLTool.getNgRenderNodeUrl(nodeId, null, true))
                        .property("link.static", URLTool.getNgRenderNodeUrl(nodeId, null, false))
                        .build())
                .build();

        send(message);
    }

    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
}
