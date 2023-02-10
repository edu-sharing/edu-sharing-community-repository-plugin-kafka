package org.edu_sharing;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/notification")
public class NotificationController {

    private final KafkaTemplate<String, Notification> kafkaTemplate;

    @PostMapping
    public  void publish(@RequestBody NotificationRequest request){
        kafkaTemplate.send(AppConstants.TOPIC_NAME_NOTIFICATION, new Notification("some test", request.message()));
    }
}
