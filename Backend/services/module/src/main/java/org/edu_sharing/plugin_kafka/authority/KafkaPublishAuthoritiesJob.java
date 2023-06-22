package org.edu_sharing.plugin_kafka.authority;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.jobs.helper.NodeRunner;
import org.edu_sharing.repository.server.jobs.quartz.AbstractJobMapAnnotationParams;
import org.edu_sharing.repository.server.jobs.quartz.annotation.JobDescription;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Slf4j
@JobDescription(description = "This job publishes all authorities to the Kafka topic")
public class KafkaPublishAuthoritiesJob  extends AbstractJobMapAnnotationParams {


    @Autowired
    private KafkaAuthorityPublisherService publisherService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        AuthenticationUtil.runAsSystem(() -> {
            publisherService.publishAllAuthorities();
            return null;
        });
    }


}
