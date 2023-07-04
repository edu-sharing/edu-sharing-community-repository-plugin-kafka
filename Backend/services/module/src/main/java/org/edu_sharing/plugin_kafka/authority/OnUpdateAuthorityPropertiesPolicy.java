package org.edu_sharing.plugin_kafka.authority;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OnUpdateAuthorityPropertiesPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnDeleteNodePolicy {

    private final PolicyComponent policyComponent;
    private final KafkaAuthorityPublisherService kafkaAuthorityPublisherService;


    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_AUTHORITY, new JavaBehaviour(this, "onUpdateProperties"));
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, ContentModel.TYPE_AUTHORITY, new JavaBehaviour(this, "onDeleteNodePolicy"));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        log.info("Notify user props changed");
        try {
            kafkaAuthorityPublisherService.publishAuthority(after);
        } catch (IOException e) {
            log.error("Could not send notification for authority property changed!", e);
        }
    }

    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
        if(!isNodeArchived){
            log.info("Notify user props deleted");
            kafkaAuthorityPublisherService.deleteAuthority(childAssocRef.getChildRef());
        }
    }
}
