package org.edu_sharing.plugin_kafka;

import org.edu_sharing.restservices.about.v1.model.PluginInfo;
import org.springframework.stereotype.Component;

@Component
public class KafkaPluginInfo implements PluginInfo {
    public String getId() {
        return "kafka-notification-plugin";
    }
}
