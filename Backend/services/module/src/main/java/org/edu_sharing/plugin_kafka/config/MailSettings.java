package org.edu_sharing.plugin_kafka.config;

import lombok.Data;
import org.edu_sharing.lightbend.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "repository.mail")
public class MailSettings {
    private String from;
    private String addReplyTo;
    private Report report;
}

@Data
class Report {
    private String receiver;
}
