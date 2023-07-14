package org.edu_sharing.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.edu_sharing.notification.event.NotificationEvent;
import org.edu_sharing.notification.mapper.RestNotificationMapper;
import org.edu_sharing.userData.UserData;
import org.edu_sharing.userData.UserDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.edu_sharing.rest.notification.event.NotificationEventDTO;
import org.edu_sharing.rest.notification.data.StatusDTO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/notification")
@Tag(name = "Notifications", description = "Notification Resources")
public class NotificationController {
    private final NotificationManager notificationManager;
    private final UserDataService userDataService;

    @PostMapping
    @Operation(summary = "Endpoint to publish notifications to kafka and to send by notification services",
            responses = @ApiResponse(responseCode = "200", description = "published notification"))
    public void send(@RequestBody org.edu_sharing.kafka.notification.event.NotificationEventDTO request) {
        notificationManager.publishNotificationToKafka(request);
    }


    @GetMapping
    @Parameters({
            @Parameter(name = "receiverId", description = "receiver identifier",
                    in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "status", description = "status (or conjunction)",
                    in = ParameterIn.QUERY, content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatusDTO.class)))),
            @Parameter(name = "page", description = "page number",
                    in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "page size",
                    in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "25")),
            @Parameter(name = "sort", description = "Sorting criteria in the format: property(,asc|desc)(,ignoreCase). "
                    + "Default sort order is ascending. Multiple sort criteria are supported."
                    , in = ParameterIn.QUERY, content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
    })
    @Operation(summary = "Retrieve stored notification, filtered by receiver and status",
            responses = @ApiResponse(responseCode = "200",
                    description = "get the received notifications",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponsePage.class))))
    public Page<NotificationEventDTO> getNotifications(
            @RequestParam(required = false) String receiverId,
            @RequestParam(required = false) List<StatusDTO> status,
            @Parameter(hidden = true)
            @PageableDefault(size = 25)
            Pageable pageable) {

        Page<NotificationEvent> notifications = notificationManager.getAllNotifications(
                receiverId,
                status != null ? status.stream().map(RestNotificationMapper::map).collect(Collectors.toList()) : null,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort()
                )
        );
        List<String> userIds = Stream.concat(notifications.stream().map(NotificationEvent::getReceiverId), notifications.stream().map(NotificationEvent::getCreatorId))
                .distinct()
                .collect(Collectors.toList());
        Map<String, UserData> userDataAsMap = userDataService.getUserDataAsMap(userIds);
        RestNotificationMapper restNotificationMapper = new RestNotificationMapper(userDataAsMap);
        return new NotificationResponsePage(notifications.map(restNotificationMapper::map));
    }

    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "receiverId", description = "receiver identifier",
                    in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "status", description = "status (or conjunction)",
                    in = ParameterIn.QUERY, content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatusDTO.class)))),
            @Parameter(name = "page", description = "page number",
                    in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "page size",
                    in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "25")),
            @Parameter(name = "sort", description = "Sorting criteria in the format: property(,asc|desc)(,ignoreCase). "
                    + "Default sort order is ascending. Multiple sort criteria are supported."
                    , in = ParameterIn.QUERY, content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
    })
    @Operation(summary = "Retrieve stored notification, filtered by receiver and status",
            responses = @ApiResponse(responseCode = "200",
                    description = "get the received notifications",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponsePage.class))))
    public NotificationEventDTO getNotification(@PathVariable String id) {

        NotificationEvent notification = notificationManager.getNotification(id);

        List<String> userIds = new ArrayList<>(Arrays.asList(notification.getCreatorId(), notification.getReceiverId()));
        Map<String, UserData> userDataAsMap = userDataService.getUserDataAsMap(userIds);

        RestNotificationMapper restNotificationMapper = new RestNotificationMapper(userDataAsMap);
        return restNotificationMapper.map(notification);


    }

    //Wrapper class
    static class NotificationResponsePage extends PageImpl<NotificationEventDTO> {
        public NotificationResponsePage(Page<NotificationEventDTO> page) {
            super(page.getContent(), page.getPageable(), page.getTotalElements());
        }
    }


    @PatchMapping("/status")
    @Operation(summary = "Endpoint to update the notification status",
            responses = @ApiResponse(responseCode = "200",
                    description = "set notification status",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationEventDTO.class))))
    public NotificationEventDTO updateStatusByNotificationId(
            @RequestParam String id,
            @RequestParam StatusDTO status) {

        NotificationEvent notification = notificationManager.setStatusByNotificationId(id, RestNotificationMapper.map(status));
        Map<String, UserData> userDataAsMap = userDataService.getUserDataAsMap(Stream.of(notification.getCreatorId(), notification.getReceiverId()).distinct().collect(Collectors.toList()));
        return new RestNotificationMapper(userDataAsMap).map(notification);
    }

    @PatchMapping("/receiver/status")
    @Operation(summary = "Endpoint to update all notification status of a receiver",
            responses = @ApiResponse(responseCode = "200", description = "set notification status"))
    public void updateStatusByReceiverId(@RequestParam String receiverId, @RequestParam List<StatusDTO> oldStatus, @RequestParam StatusDTO newStatus) {
        notificationManager.setStatusByReceiverId(receiverId, oldStatus.stream().map(RestNotificationMapper::map).collect(Collectors.toList()), RestNotificationMapper.map(newStatus));
    }

    @DeleteMapping
    @Operation(summary = "Endpoint to delete notification by id",
            responses = @ApiResponse(responseCode = "200", description = "deleted notification"))
    public void deleteNotification(@RequestParam String id) {
        notificationManager.deleteNotification(id);
    }


}
