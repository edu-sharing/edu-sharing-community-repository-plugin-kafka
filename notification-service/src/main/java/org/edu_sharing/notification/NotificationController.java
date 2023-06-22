package org.edu_sharing.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.edu_sharing.kafka.notification.events.NotificationEventDTO;
import org.edu_sharing.kafka.notification.events.data.Status;
import org.edu_sharing.notification.model.NotificationEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/notification")
public class NotificationController {
    private final NotificationManager notificationManager;
    private final ObjectMapper mapper;

    @PostMapping
    public void send(@RequestBody NotificationEventDTO request) {
        notificationManager.publishNotificationToKafka(request);
    }


    @GetMapping
    public ResponseEntity<Slice<NotificationEventDTO>> getNotifications(
            @RequestParam(required = false) String creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Slice<NotificationEvent> notifications;
        if(StringUtils.isNotBlank(creatorId)){
            notifications = notificationManager.getNotificationsByCreatorId(creatorId, PageRequest.of(page, size));
        }else{
            notifications = notificationManager.getAllNotifications(PageRequest.of(page, size));
        }
        return new ResponseEntity<>(notifications.map(x->mapper.convertValue(x, NotificationEventDTO.class)) , HttpStatus.OK);
    }

    @PatchMapping("/updateStatus")
    public ResponseEntity<NotificationEventDTO> updateStatus(
            @RequestParam String id,
            @RequestParam Status status){

        try {
            NotificationEvent notification = notificationManager.setStatus(id, status);
            return new ResponseEntity<>(mapper.convertValue(notification, NotificationEventDTO.class), HttpStatus.OK);
        }catch (ResourceNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/removeUserName")
    public void removeUserName(@RequestParam String userId){
        notificationManager.removeUserName(userId);
    }

    @DeleteMapping
    public void deleteNotification(@RequestParam String id) {
        notificationManager.deleteNotification(id);
    }





}
