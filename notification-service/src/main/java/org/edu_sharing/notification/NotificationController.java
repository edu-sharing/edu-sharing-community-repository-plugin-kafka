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
import org.edu_sharing.notification.mapper.NotificationMapper;
import org.edu_sharing.notification.model.NotificationEvent;
import org.edu_sharing.service.notification.events.NotificationEventDTO;
import org.edu_sharing.service.notification.events.data.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/notification")
@Tag(name = "Notifications", description = "Notification Resources")
public class NotificationController {
    private final NotificationManager notificationManager;

    @PostMapping
    @Operation(summary = "Endpoint to publish notifications to kafka and to send by notification services",
            responses = @ApiResponse(responseCode = "200", description = "published notification"))
    public void send(@RequestBody NotificationEventDTO request) {
        notificationManager.publishNotificationToKafka(request);
    }


    @GetMapping
    @Parameters({
            @Parameter(name = "receiverId", description = "receiver identifier",
                    in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "status", description = "status (or conjunction)",
                    in = ParameterIn.QUERY, content = @Content(array = @ArraySchema(schema =@Schema(implementation = Status.class)))),
            @Parameter(name = "page", description = "page number",
                    in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "page size",
                    in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "25")),
            @Parameter(name = "sort", description = "Sorting criteria in the format: property(,asc|desc). "
                    + "Default sort order is ascending. " + "Multiple sort criteria are supported."
                    ,in = ParameterIn.QUERY , content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
    })
    @Operation(summary = "Retrieve stored notification, filtered by receiver and status",
            responses = @ApiResponse(responseCode = "200",
                    description = "get the received notifications",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponsePage.class))))
    public Page<NotificationEventDTO> getNotifications(
            @RequestParam(required = false) String receiverId,
            @RequestParam(required = false) List<Status> status,
            @Parameter(hidden = true)
            @PageableDefault(size = 25)
            Pageable pageable) {

        Page<NotificationEvent> notifications = notificationManager.getAllNotifications(
                receiverId, status,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort()
                )
        );
        return new NotificationResponsePage(notifications.map(NotificationMapper::map));
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
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NotificationEventDTO.class)))))
    public NotificationEventDTO updateStatus(
            @RequestParam String id,
            @RequestParam Status status) {

        NotificationEvent notification = notificationManager.setStatus(id, status);
        return NotificationMapper.map(notification);
    }

    @DeleteMapping
    @Operation(summary = "Endpoint to delete notification by id",
            responses = @ApiResponse(responseCode = "200", description = "deleted notification"))
    public void deleteNotification(@RequestParam String id) {
        notificationManager.deleteNotification(id);
    }


}
