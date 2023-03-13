package org.edu_sharing;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.edu_sharing.messages.BaseMessage;
import org.edu_sharing.messages.data.Status;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public void send(@RequestBody BaseMessage request) {
        notificationService.sendNotification(request);
    }


    @GetMapping
    public ResponseEntity<Slice<BaseMessage>> getNotifications(
            @RequestParam(required = false) String creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Slice<BaseMessage> notifications;
        if(StringUtils.isNotBlank(creatorId)){
            notifications = notificationService.getNotificationsByCreatorId(creatorId, PageRequest.of(page, size));
        }else{
            notifications = notificationService.getAllNotifications(PageRequest.of(page, size));
        }
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }

    @PatchMapping("/updateStatus")
    public ResponseEntity<BaseMessage> updateStatus(
            @RequestParam String id,
            @RequestParam Status status){

        try {
            BaseMessage message = notificationService.setStatus(id, status);
            return new ResponseEntity<>(message, HttpStatus.OK);
        }catch (ResourceNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/removeUserName")
    public void removeUserName(@RequestParam String userId){
        notificationService.removeUserName(userId);
    }

    @DeleteMapping
    public void deleteNotification(@RequestParam String id) {
        notificationService.deleteNotification(id);
    }





}
