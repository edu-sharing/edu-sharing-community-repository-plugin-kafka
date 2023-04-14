package org.edu_sharing.plugin_kafka.config;

import com.typesafe.config.Optional;
import lombok.Data;

@Data
public class Report {
    @Optional
    private String receiver;
}
