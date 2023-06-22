package org.edu_sharing.plugin_kafka.authority;

import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.repository.server.update.UpdateRoutine;
import org.edu_sharing.repository.server.update.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@UpdateService
public class KafkaPublishAuthoritiesUpdate {


    private final KafkaAuthorityPublisherService publisherService;

    @Autowired
    public KafkaPublishAuthoritiesUpdate(KafkaAuthorityPublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @UpdateRoutine(
            id = "KafkaPublishAuthoritiesUpdate",
            description = "This update will publish all authorities to the kafka broker",
            order = 300000,
            auto = true,
            isNonTransactional = true
    )
    public void execute() {
        publisherService.publishAllAuthorities();
    }
}
