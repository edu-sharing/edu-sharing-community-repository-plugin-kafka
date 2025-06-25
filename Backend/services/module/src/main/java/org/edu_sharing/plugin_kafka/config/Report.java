package org.edu_sharing.plugin_kafka.config;

import com.typesafe.config.Optional;
import lombok.Data;

import java.util.List;

@Data
public class Report {
    @Optional
    private List<String> receivers;
}
