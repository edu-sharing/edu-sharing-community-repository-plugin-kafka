package org.edu_sharing;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {


    @KafkaListener(
            topics = AppConstants.TOPIC_NAME_NOTIFICATION,
            groupId = AppConstants.GROUP_ID
    )
    void listener(Notification data){
        System.out.println("Listener received issue: \"" + data.getIssue() + "\" with message: \"" + data.getMessage() + "\"");
    }
}
