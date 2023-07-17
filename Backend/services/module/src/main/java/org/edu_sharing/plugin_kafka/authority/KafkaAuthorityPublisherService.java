
package org.edu_sharing.plugin_kafka.authority;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.edu_sharing.kafka.user.NotificationIntervalDTO;
import org.edu_sharing.kafka.user.UserDataDTO;
import org.edu_sharing.plugin_kafka.kafka.KafkaTemplate;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.AuthenticationToolAPI;
import org.edu_sharing.service.notification.NotificationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Slf4j
@Component
public class KafkaAuthorityPublisherService {

    private final KafkaTemplate<String, UserDataDTO> kafkaUserTemplate;
    private final NodeService nodeService;
    private final AuthorityService authorityService;

    @Autowired
    public KafkaAuthorityPublisherService(@Qualifier("kafkaUserTemplate") KafkaTemplate<String, UserDataDTO> kafkaUserTemplate,
                                          NodeService nodeService,
                                          AuthorityService authorityService) {
        this.kafkaUserTemplate = kafkaUserTemplate;
        this.nodeService = nodeService;
        this.authorityService = authorityService;
    }

    public void publishAuthority(Map<QName, Serializable> properties) throws IOException {
        log.info("publish authority");
        ObjectMapper mapper = new ObjectMapper();
        String notificationPrefs = (String) properties.get(QName.createQName(CCConstants.CCM_PROP_PERSON_NOTIFICATION_PREFERENCES));


        UserDataDTO userDataDTO = new UserDataDTO();
        if (!StringUtils.isBlank(notificationPrefs)) {
            NotificationConfig notificationConfig = mapper.readValue(notificationPrefs, NotificationConfig.class);
            userDataDTO.setAddToCollectionEvent(mapNotificationInterval(notificationConfig.getAddToCollectionEvent()));
            userDataDTO.setProposeForCollectionEvent(mapNotificationInterval(notificationConfig.getProposeForCollectionEvent()));
            userDataDTO.setCommentEvent(mapNotificationInterval(notificationConfig.getCommentEvent()));
            userDataDTO.setInviteEvent(mapNotificationInterval(notificationConfig.getInviteEvent()));
            userDataDTO.setNodeIssueEvent(mapNotificationInterval(notificationConfig.getNodeIssueEvent()));
            userDataDTO.setRatingEvent(mapNotificationInterval(notificationConfig.getRatingEvent()));
            userDataDTO.setWorkflowEvent(mapNotificationInterval(notificationConfig.getWorkflowEvent()));
            userDataDTO.setMetadataSuggestionEvent(mapNotificationInterval(notificationConfig.getMetadataSuggestionEvent()));
        }

        String authorityName = (String) properties.get(QName.createQName(CCConstants.CM_PROP_AUTHORITY_NAME));
        if (AuthorityType.GROUP == AuthorityType.getAuthorityType(authorityName)) {
            userDataDTO.setFirstName("");
            userDataDTO.setLastName((String) properties.get(QName.createQName(CCConstants.CM_PROP_AUTHORITY_AUTHORITYDISPLAYNAME)));
            userDataDTO.setEmail((String) properties.get(QName.createQName(CCConstants.CCM_PROP_GROUPEXTENSION_GROUPEMAIL)));
        } else {
            userDataDTO.setFirstName((String) properties.get(QName.createQName(CCConstants.CM_PROP_PERSON_FIRSTNAME)));
            userDataDTO.setLastName((String) properties.get(QName.createQName(CCConstants.CM_PROP_PERSON_LASTNAME)));
            userDataDTO.setEmail((String) properties.get(QName.createQName(CCConstants.PROP_USER_EMAIL)));
        }

//        userDataDTO.setLocale(new AuthenticationToolAPI().getCurrentLocale());
        userDataDTO.setLocale("de_DE"); // TODO we need the local of the users but this isn't stored in the db
        try {
            kafkaUserTemplate.sendDefault(properties.get(QName.createQName(CCConstants.SYS_PROP_NODE_UID)).toString(), userDataDTO);
        } catch (Exception ex){
            log.error("Publish authority {}", properties.get(QName.createQName(CCConstants.SYS_PROP_NODE_UID)).toString(), ex);
        }
    }

    private static NotificationIntervalDTO mapNotificationInterval(NotificationConfig.NotificationInterval notificationConfigInterval) {
        return NotificationIntervalDTO.valueOf(notificationConfigInterval.name());
    }

    public void publishAllAuthorities() {
        List<String> allAuthorities = new ArrayList<>();
        allAuthorities.addAll(getAuthorities(AuthorityType.GROUP));
        allAuthorities.addAll(getAuthorities(AuthorityType.USER));
        allAuthorities.stream()
                .map(authorityService::getAuthorityNodeRef)
                .filter(Objects::nonNull)
                .forEach(this::publishAuthority);
        log.info("Processed {} authorities", allAuthorities.size());
    }

    private List<String> getAuthorities(AuthorityType type) {
        int pageSize = 100; // Anzahl der Autoritäten pro Seite

        List<String> allAuthorities = new ArrayList<>();
        PagingRequest pagingRequest = new PagingRequest(pageSize);
        PagingResults<String> pagingResults;
        do {
            pagingResults = authorityService.getAuthorities(type, null, null, false, false, pagingRequest);
            allAuthorities.addAll(pagingResults.getPage());
            pagingRequest = new PagingRequest(pageSize, pagingResults.getQueryExecutionId());
        } while (pagingResults.hasMoreItems());
        return allAuthorities;
    }

    private void publishAuthority(NodeRef nodeRef) {
        log.info("Handle authority: {}", nodeRef);
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        String authorityName = (String) properties.get(QName.createQName(CCConstants.CM_PROP_AUTHORITY_NAME));
        if (StringUtils.isBlank(authorityName)) {
            authorityName = (String) properties.get(QName.createQName(CCConstants.CM_PROP_PERSON_USERNAME));
        }
        try {
            publishAuthority(properties);
            log.info("Published authority: {}", authorityName);
        } catch (IOException e) {
            log.error("Could not publish authority: {}", authorityName, e);
        }
    }

    public void deleteAuthority(NodeRef nodeRef) {
        log.info("Handle delete authority: {}", nodeRef);

        kafkaUserTemplate.sendDefault(nodeRef.getId(), null);
        log.info("Published deleted authority: {}", nodeRef);
    }
}
