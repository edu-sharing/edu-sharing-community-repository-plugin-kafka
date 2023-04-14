package org.edu_sharing.plugin_kafka.config;

import lombok.Data;
import lombok.NonNull;
import org.edu_sharing.lightbend.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "repository.mail")
public class MailSettings {
    private String from;
    private Report report;
}