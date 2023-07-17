package org.edu_sharing.plugin_kafka.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.client.http.HttpStatusCodes;
import com.sun.star.lang.IllegalArgumentException;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.edu_sharing.alfresco.workspace_administration.NodeServiceInterceptor;
import org.edu_sharing.kafka.notification.data.*;
import org.edu_sharing.kafka.notification.event.*;
import org.edu_sharing.metadataset.v2.MetadataWidget;
import org.edu_sharing.plugin_kafka.config.KafkaSettings;
import org.edu_sharing.plugin_kafka.config.MailSettings;
import org.edu_sharing.plugin_kafka.config.Report;
import org.edu_sharing.plugin_kafka.kafka.KafkaTemplate;
import org.edu_sharing.plugin_kafka.kafka.SendResult;
import org.edu_sharing.plugin_kafka.kafka.support.JacksonUtils;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.client.tools.I18nAngular;
import org.edu_sharing.repository.server.AuthenticationToolAPI;
import org.edu_sharing.repository.server.tools.I18nServer;
import org.edu_sharing.repository.server.tools.URLTool;
import org.edu_sharing.repository.server.tools.mailtemplates.MailTemplate;
import org.edu_sharing.restservices.mds.v1.model.MdsValue;
import org.edu_sharing.service.InsufficientPermissionException;
import org.edu_sharing.service.authority.AuthorityService;
import org.edu_sharing.service.authority.AuthorityServiceHelper;
import org.edu_sharing.service.notification.NotificationService;
import org.edu_sharing.service.notification.Status;
import org.edu_sharing.service.rating.RatingDetails;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service("kafkaNotificationService")
public class KafkaNotificationService implements NotificationService {

    @Setter
    @Autowired
    @Qualifier("kafkaNotificationTemplate")
    private KafkaTemplate<String, NotificationEventDTO> kafkaNotificationTemplate;

    @Setter
    @Autowired
    private AuthorityService authorityService;

    @Setter
    @Autowired
    private MailSettings mailSettings;

    @Setter
    @Autowired
    private KafkaSettings kafkaSettings;

    public CompletableFuture<SendResult<String, NotificationEventDTO>> send(NotificationEventDTO notificationMessage) {
        try {
            notificationMessage.setId(generateMessageId());
            notificationMessage.setStatus(StatusDTO.NEW);
            notificationMessage.setTimestamp(DateTime.now().toDate());
            return kafkaNotificationTemplate.sendDefault(notificationMessage.getId(), notificationMessage);
        }catch (Exception ex){
            log.error("Error on sending notification: {} ", notificationMessage, ex);
            return null;
        }
    }

    @Override
    public void notifyNodeIssue(String nodeId, String reason, String nodeType, List<String> aspects, Map<String, Object> nodeProperties, String userEmail, String userComment) throws Throwable {

        if (Optional.of(mailSettings).map(MailSettings::getReport).map(Report::getReceiver).map(StringUtils::isBlank).orElse(true)) {
            throw new IllegalArgumentException("No report receiverAuthority is set in the configuration");
        }


        send(new NodeIssueEventDTO(
                null,
                null,
                "system",
                "report",
                null,
                createNodeData(nodeId, nodeType, aspects, getSimplifiedNodeProperties(nodeProperties)),
                userEmail,
                reason,
                reason
        ));
    }

    @Override

    public void notifyWorkflowChanged(String nodeId, String nodeType, List<String> aspects, Map<String, Object> nodeProperties, String receiverAuthority, String comment, String status) {
        String senderId = authorityService.getAuthorityNodeRef(new AuthenticationToolAPI().getCurrentUser()).getId();
        String receiverId = authorityService.getAuthorityNodeRef(receiverAuthority).getId();

        // TODO group handling

        send(new WorkflowEventDTO(
                null,
                null,
                senderId,
                receiverId,
                null,
                createNodeData(nodeId, nodeType, aspects, getSimplifiedNodeProperties(nodeProperties)),
                comment,
                I18nAngular.getTranslationAngular("common", "WORKFLOW." + status)
        ));
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
    public void notifyPermissionChanged(String senderAuthority, String receiverAuthority, String nodeId, String nodeType, List<String> aspects, Map<String, Object> nodeProperties, String[] permissions, String mailText) throws Throwable {

        // if the receiverAuthority is the creator itself, skip it (because it is automatically added)
        String nodeCreator = (String) nodeProperties.get(CCConstants.CM_PROP_C_CREATOR);
        if (receiverAuthority.equals(nodeCreator)) {
            return;
        }

        String senderId = authorityService.getAuthorityNodeRef(senderAuthority).getId();
        String receiverId = authorityService.getAuthorityNodeRef(receiverAuthority).getId();

        // TODO group handling

        String internalNodeType = (String) nodeProperties.get(CCConstants.NODETYPE);
        String invitationType = "invited";
        if (internalNodeType.equals(CCConstants.CCM_TYPE_MAP) && aspects.contains(CCConstants.CCM_ASPECT_COLLECTION)) {
            invitationType = "invited_collection";
        }

        String name = internalNodeType.equals(CCConstants.CCM_TYPE_IO)
                ? (String) nodeProperties.get(CCConstants.LOM_PROP_GENERAL_TITLE)
                : (String) nodeProperties.get(CCConstants.CM_PROP_C_TITLE);

        if (StringUtils.isBlank(name)) {
            name = (String) nodeProperties.get(CCConstants.CM_NAME);
        }

        List<PermissionDTO> permissionList = Arrays.stream(permissions)
                .filter(perm -> !(CCConstants.CCM_VALUE_SCOPE_SAFE.equals(NodeServiceInterceptor.getEduSharingScope()) && Objects.equals(CCConstants.PERMISSION_CC_PUBLISH, perm)))
                .map(perm -> new PermissionDTO(perm,
                        I18nServer.getTranslationDefaultResourcebundle(I18nServer.getPermissionDescription(perm), new AuthenticationToolAPI().getCurrentLocale())))
                .collect(Collectors.toList());

        if (CCConstants.CCM_VALUE_SCOPE_SAFE.equals(NodeServiceInterceptor.getEduSharingScope())) {
            send(new InviteSafeEventDTO(
                    null,
                    null,
                    senderId,
                    receiverId,
                    null,
                    createNodeData(nodeId, nodeType, aspects, getSimplifiedNodeProperties(nodeProperties)),
                    name,
                    mailText,
                    permissionList
            ));
        } else {
            send(new InviteEventDTO(
                    null,
                    null,
                    senderId,
                    receiverId,
                    null,
                    createNodeData(nodeId, nodeType, aspects, getSimplifiedNodeProperties(nodeProperties)),
                    name,
                    invitationType,
                    mailText,
                    permissionList
            ));
        }
    }


    @Override
    public void notifyMetadataSetSuggestion(MdsValue mdsValue, MetadataWidget widgetDefinition, List<String> nodes, List<String> nodeTypes, List<List<String>> aspects, List<Map<String, Object>> nodePropertiesList) throws Throwable {
        String senderId = authorityService.getAuthorityNodeRef(new AuthenticationToolAPI().getCurrentUser()).getId();

        String[] receivers = widgetDefinition.getSuggestionReceiver().split(",");

        // TODO group handling
        for (String receiverAuthority : receivers) {
            String receiverId = authorityService.getAuthorityNodeRef(receiverAuthority).getId();
            if (nodes.size() == 0) {
                send(new MetadataSuggestionEventDTO(
                        null,
                        null,
                        senderId,
                        receiverId,
                        null,
                        null,
                        mdsValue.getId(),
                        mdsValue.getCaption(),
                        mdsValue.getParent(),
                        mdsValue.getParent() == null ? null : widgetDefinition.getValuesAsMap().get(mdsValue.getParent()).getCaption(),
                        new WidgetDataDTO(
                                widgetDefinition.getId(),
                                widgetDefinition.getCaption()
                        )
                ));
            }

            for (int i = 0; i < nodes.size(); i++) {
                send(new MetadataSuggestionEventDTO(
                        null,
                        null,
                        senderId,
                        receiverId,
                        null,
                        createNodeData(nodes.get(i), nodeTypes.get(i), aspects.get(i), getSimplifiedNodeProperties(nodePropertiesList.get(i))),
                        mdsValue.getId(),
                        mdsValue.getCaption(),
                        mdsValue.getParent(),
                        mdsValue.getParent() == null ? null : widgetDefinition.getValuesAsMap().get(mdsValue.getParent()).getCaption(),
                        new WidgetDataDTO(
                                widgetDefinition.getId(),
                                widgetDefinition.getCaption()
                        )
                ));
            }

        }
    }

    @Override
    public void notifyComment(String node, String comment, String commentReference, String nodeType, List<String> aspects, Map<String, Object> nodeProperties, Status status) {
        String receiverAuthority = (String) nodeProperties.get(CCConstants.CM_PROP_C_CREATOR);


        String senderId = authorityService.getAuthorityNodeRef(new AuthenticationToolAPI().getCurrentUser()).getId();
        String receiverId = authorityService.getAuthorityNodeRef(receiverAuthority).getId();

        if(Objects.equals(receiverId, senderId)){
            return;
        }

        send(new CommentEventDTO(
                null,
                null,
                senderId,
                receiverId,
                null,
                createNodeData(node, nodeType, aspects, getSimplifiedNodeProperties(nodeProperties)),
                comment,
                commentReference,
                status.toString()
        ));
    }

    @Override
    public void notifyProposeForCollection(String collectionId, String refNodeId, String collectionType, List<String> collectionAspects, Map<String, Object> collectionProperties, String nodeType, List<String> nodeAspects, Map<String, Object> nodeProperties, Status status) {

        String receiverAuthority = (String) collectionProperties.get(CCConstants.CM_PROP_C_CREATOR);
        String senderAuthority = new AuthenticationToolAPI().getCurrentUser();

        String senderId = authorityService.getAuthorityNodeRef(senderAuthority).getId();
        String receiverId = authorityService.getAuthorityNodeRef(receiverAuthority).getId();

        if (Objects.equals(senderId, receiverId)) {
            return;
        }

        send(new ProposeForCollectionEventDTO(
                null,
                null,
                senderId,
                receiverId,
                null,
                createNodeData(refNodeId, nodeType, nodeAspects, getSimplifiedNodeProperties(nodeProperties)),
                createCollectionDTO(refNodeId, collectionType, nodeAspects, getSimplifiedNodeProperties(nodeProperties))
        ));
    }

    @Override
    public void notifyAddCollection(String collectionId, String refNodeId, String collectionType, List<String> collectionAspects, Map<String, Object> collectionProperties, String nodeType, List<String> nodeAspects, Map<String, Object> nodeProperties, Status status) {

        String receiverAuthority = (String) collectionProperties.get(CCConstants.CM_PROP_C_CREATOR);
        String senderAuthority = new AuthenticationToolAPI().getCurrentUser();

        String senderId = authorityService.getAuthorityNodeRef(senderAuthority).getId();
        String receiverId = authorityService.getAuthorityNodeRef(receiverAuthority).getId();

        if (Objects.equals(senderId, receiverId)) {
            return;
        }

        send(new AddToCollectionEventDTO(
                null,
                null,
                senderId,
                receiverId,
                null,
                createNodeData(refNodeId, nodeType, nodeAspects, getSimplifiedNodeProperties(nodeProperties)),
                createCollectionDTO(refNodeId, collectionType, nodeAspects, getSimplifiedNodeProperties(nodeProperties))
        ));
    }

    @Override
    public void notifyRatingChanged(String nodeId, String nodeType, List<String> aspects, Map<String, Object> nodeProperties, Double rating, RatingDetails accumulatedRatings, Status removed) {
        String receiverAuthority = (String) nodeProperties.get(CCConstants.CM_PROP_C_CREATOR);
        String senderId = authorityService.getAuthorityNodeRef(new AuthenticationToolAPI().getCurrentUser()).getId();
        String receiverId = authorityService.getAuthorityNodeRef(receiverAuthority).getId();

        if (Optional.of(mailSettings).map(MailSettings::getFrom).map(StringUtils::isBlank).orElse(true)) {
            log.warn("notifyRatingChanged: No send mail receiverAuthority is set in the configuration");
            return;
        }

        if (Objects.equals(senderId, receiverId)) {
            return;
        }

        send(new RatingEventDTO(
                null,
                null,
                "system",
                receiverId,
                null,
                createNodeData(nodeId, nodeType, aspects, getSimplifiedNodeProperties(nodeProperties)),
                rating,
                accumulatedRatings.getOverall().getSum(),
                accumulatedRatings.getOverall().getCount()
        ));
    }


    @Override
    public Page<org.edu_sharing.rest.notification.event.NotificationEventDTO> getNotifications(String receiverId, List<org.edu_sharing.rest.notification.data.StatusDTO> status, Pageable pageable) throws IOException, InsufficientPermissionException {
        try {
            URIBuilder builder = new URIBuilder(kafkaSettings.getNotificationServiceUrl());
            builder.setPath("/api/v1/notification");

            if ("-me-".equals(receiverId)) {
                receiverId = authorityService.getAuthorityNodeRef(new AuthenticationToolAPI().getCurrentUser()).getId();
            }

            if(!AuthorityServiceHelper.isAdmin()){
                String currentUser = authorityService.getAuthorityNodeRef(new AuthenticationToolAPI().getCurrentUser()).getId();
                if(!currentUser.equals(receiverId)){
                    throw new InsufficientPermissionException("You haven't enough permission to see notifications");
                }
            }


            builder.setParameter("receiverId", receiverId);
            builder.setParameter("status", StringUtils.join(status, ","));
            builder.setParameter("page", String.valueOf(pageable.getPageNumber()));
            builder.setParameter("size", String.valueOf(pageable.getPageSize()));
            if (!pageable.getSort().isEmpty()) {
                pageable.getSort().forEach(order -> {
                    builder.setParameter("sort", order.getProperty() + "," + order.getDirection().toString());
                });
            }

            HttpGet request = new HttpGet(builder.build());
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                CloseableHttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();

                if (entity == null) {
                    return null;
                }

                String content = EntityUtils.toString(entity, "UTF-8");
                if (response.getStatusLine().getStatusCode() != HttpStatusCodes.STATUS_CODE_OK) {
                    throw new HttpException(content);
                }

                NotificationResponsePage notificationEventDTOS = JacksonUtils.enhancedObjectMapper().readValue(content, NotificationResponsePage.class);
                notificationEventDTOS.setPageable(pageable);
                return notificationEventDTOS;
            }
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public org.edu_sharing.rest.notification.event.NotificationEventDTO getNotification(String id) throws IOException {
        try {
            URIBuilder builder = new URIBuilder(kafkaSettings.getNotificationServiceUrl());
            builder.setPath(String.format("/api/v1/notification/%s", id));

            HttpGet request = new HttpGet(builder.build());
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                CloseableHttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();

                if (entity == null) {
                    return null;
                }

                String content = EntityUtils.toString(entity, "UTF-8");
                if (response.getStatusLine().getStatusCode() != HttpStatusCodes.STATUS_CODE_OK) {
                    throw new HttpException(content);
                }

                return JacksonUtils.enhancedObjectMapper().readValue(content, org.edu_sharing.rest.notification.event.NotificationEventDTO.class);
            }
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public org.edu_sharing.rest.notification.event.NotificationEventDTO setNotificationStatusByNotificationId(String id, org.edu_sharing.rest.notification.data.StatusDTO status) throws IOException, InsufficientPermissionException {
        try {
            org.edu_sharing.rest.notification.event.NotificationEventDTO notification = getNotification(id);
            String currentUser = authorityService.getAuthorityNodeRef(new AuthenticationToolAPI().getCurrentUser()).getId();
            if(!currentUser.equals(notification.getReceiver().getId())){
                throw new InsufficientPermissionException("Notification status of can only be set by it's receiver!");
            }

            URIBuilder builder = new URIBuilder(kafkaSettings.getNotificationServiceUrl());
            builder.setPath("/api/v1/notification/status");
            builder.setParameter("id", id);
            builder.setParameter("status", status.toString());

            HttpPatch request = new HttpPatch(builder.build());
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                CloseableHttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();

                if (entity == null) {
                    return null;
                }

                String content = EntityUtils.toString(entity, "UTF-8");
                if (response.getStatusLine().getStatusCode() != HttpStatusCodes.STATUS_CODE_OK) {
                    throw new HttpException(content);
                }

                return JacksonUtils.enhancedObjectMapper().readValue(content, org.edu_sharing.rest.notification.event.NotificationEventDTO.class);
            }
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setNotificationStatusByReceiverId(String receiverId, List<org.edu_sharing.rest.notification.data.StatusDTO> oldStatusList, org.edu_sharing.rest.notification.data.StatusDTO newStatus) throws IOException, InsufficientPermissionException {
        try {
            String currentUser = authorityService.getAuthorityNodeRef(new AuthenticationToolAPI().getCurrentUser()).getId();
            if(!currentUser.equals(receiverId)){
                throw new InsufficientPermissionException("Notification status of can only be set by it's receiver!");
            }

            URIBuilder builder = new URIBuilder(kafkaSettings.getNotificationServiceUrl());
            builder.setPath("/api/v1/notification/receiver/status");
            builder.setParameter("id", receiverId);
            oldStatusList.forEach(x -> builder.setParameter("oldStatus", x.toString()));
            builder.setParameter("oldStatus", newStatus.toString());

            HttpPatch request = new HttpPatch(builder.build());
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                CloseableHttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();

                if (entity == null) {
                    return;
                }

                String content = EntityUtils.toString(entity, "UTF-8");
                if (response.getStatusLine().getStatusCode() != HttpStatusCodes.STATUS_CODE_OK) {
                    throw new HttpException(content);
                }

            }
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteNotification(String id) throws IOException, InsufficientPermissionException {
        try {

            org.edu_sharing.rest.notification.event.NotificationEventDTO notification = getNotification(id);
            String currentUser = authorityService.getAuthorityNodeRef(new AuthenticationToolAPI().getCurrentUser()).getId();
            if(!currentUser.equals(notification.getReceiver().getId())){
                throw new InsufficientPermissionException("Notification status of can only be set by it's receiver!");
            }

            URIBuilder builder = new URIBuilder(kafkaSettings.getNotificationServiceUrl());
            builder.setPath("/api/v1/notification");
            builder.setParameter("id", id);

            HttpDelete request = new HttpDelete(builder.build());
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                CloseableHttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();

                if (entity == null) {
                    return;
                }

                String content = EntityUtils.toString(entity, "UTF-8");
                if (response.getStatusLine().getStatusCode() != HttpStatusCodes.STATUS_CODE_OK) {
                    throw new HttpException(content);
                }

            }
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }



    @Data
    static class NotificationResponsePage implements Page<org.edu_sharing.rest.notification.event.NotificationEventDTO> {
        private List<org.edu_sharing.rest.notification.event.NotificationEventDTO> content;

        private long totalElements;
        private int totalPages;
        private int number;
        private boolean last;
        private boolean first;
        private boolean hasNext;
        private boolean hasPrevious;
        private int size;

        @JsonIgnore
        private Pageable pageable;


        @JsonIgnore
        @Override
        public int getNumberOfElements() {
            return content.size();
        }

        @JsonIgnore
        @Override
        public boolean hasContent() {
            return !content.isEmpty();
        }

        @JsonIgnore
        public Sort getSort() {
            return pageable.getSort();
        }

        @JsonIgnore
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @JsonIgnore
        @Override
        public boolean hasPrevious() {
            return hasPrevious;
        }

        @JsonIgnore
        @Override
        public Pageable nextPageable() {
            return null;
        }

        @JsonIgnore
        @Override
        public Pageable previousPageable() {
            return null;
        }

        @JsonIgnore
        @Override
        public <U> Page<U> map(Function<? super org.edu_sharing.rest.notification.event.NotificationEventDTO, ? extends U> converter) {
            return null;
        }

        @JsonIgnore
        @NotNull
        @Override
        public Iterator<org.edu_sharing.rest.notification.event.NotificationEventDTO> iterator() {
            return content.iterator();
        }

        @JsonIgnore
        public boolean isEmpty() {
            return content.isEmpty();
        }
    }

    private static NodeDataDTO createNodeData(String nodeId, String type, List<String> aspects, Map<String, Object> nodeProperties) {
        HashMap<String, Object> props = new HashMap<>(nodeProperties);
        props.put("link", URLTool.getNgRenderNodeUrl(nodeId, null, true));
        props.put("link.static", URLTool.getNgRenderNodeUrl(nodeId, null, false));

        return new NodeDataDTO(
                type,
                aspects,
                props);
    }

    private static CollectionDTO createCollectionDTO(String nodeId, String type, List<String> aspects, Map<String, Object> nodeProperties) {
        HashMap<String, Object> props = new HashMap<>(nodeProperties);
        props.put("link", URLTool.getNgRenderNodeUrl(nodeId, null, true));
        props.put("link.static", URLTool.getNgRenderNodeUrl(nodeId, null, false));

        return new CollectionDTO(
                type,
                aspects,
                props);
    }


    private static Map<String, Object> getSimplifiedNodeProperties(Map<String, Object> nodeProperties) {
        return nodeProperties.entrySet().stream()
                .map(x -> new ImmutablePair<>(CCConstants.getValidLocalName(x.getKey()), x.getValue()))
                .filter(x -> StringUtils.isNoneBlank(x.getKey()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }


    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
}
