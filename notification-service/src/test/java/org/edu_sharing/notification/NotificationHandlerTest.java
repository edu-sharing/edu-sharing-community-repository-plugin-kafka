package org.edu_sharing.notification;

import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.event.NotificationEvent;
import org.edu_sharing.service.NotificationService;
import org.edu_sharing.userData.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationHandlerTest {


    @Mock
    private NotificationManager notificationManager;
    @Mock
    private UserDataService userDataService;
    @Mock
    private NotificationService notificationService;

    private NotificationHandler notificationHandler;

    @BeforeEach
    void setup() {
        List<NotificationService> notificationServices = Collections.singletonList(notificationService);
        notificationHandler = new NotificationHandler(notificationManager, notificationServices, userDataService);
    }

    @Test
    public void testResendFailedNotificationsOnStartup_NoPendingNotifications() {
        // arrange
        List<NotificationEvent> notificationEvents = Collections.emptyList();
        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);

        // act
        notificationHandler.resendFailedNotificationsOnStartup();

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, never()).saveAllNotifications(any());
        verify(notificationService, never()).send(any());
    }

    @Test
    public void testResendFailedNotificationsOnStartup_WithPendingNotifications_Immediate() {
        // arrange

        String userId1 = "user1";
        String userId2 = "user2";
        Map<String, UserData> userDataMap = Map.of(
                userId1, mock(UserData.class),
                userId2, mock(UserData.class)
        );

        List<NotificationEvent> pendingEvents = new ArrayList<>();
        NotificationEvent event1 = NotificationEvent.builder().receiverId(userId1).build();
        NotificationEvent event2 = NotificationEvent.builder().receiverId(userId2).build();

        pendingEvents.add(event1);
        pendingEvents.add(event2);

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(pendingEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId1).getNotificationInterval(event1)).thenReturn(NotificationInterval.immediately);
        when(userDataMap.get(userId2).getNotificationInterval(event2)).thenReturn(NotificationInterval.immediately);

        // act
        notificationHandler.resendFailedNotificationsOnStartup();

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, times(1)).saveAllNotifications(pendingEvents);
        verify(notificationService, times(1)).send(pendingEvents);
    }

    @Test
    public void testResendFailedNotificationsOnStartup_WithPendingNotifications_Disabled() {
        // arrange

        String userId1 = "user1";
        Map<String, UserData> userDataMap = Map.of(
                userId1, mock(UserData.class)
        );
        NotificationEvent event1 = NotificationEvent.builder().receiverId(userId1).build();


        List<NotificationEvent> pendingEvents = new ArrayList<>();
        pendingEvents.add(event1);

        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(pendingEvents);
        when(userDataMap.get(userId1).getNotificationInterval(event1)).thenReturn(NotificationInterval.disabled);

        // act
        notificationHandler.resendFailedNotificationsOnStartup();

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, times(1)).saveAllNotifications(pendingEvents);
        verify(notificationManager).saveAllNotifications(argThat((List<NotificationEvent> events) -> events.stream().allMatch(event -> event.getStatus() == Status.IGNORED)));
        verify(notificationService, never()).send(any());
    }

    @Test
    public void testHandlePendingNotification_NoPendingNotifications() {
        // arrange
        List<NotificationEvent> notificationEvents = Collections.emptyList();
        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.immediately);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, never()).saveAllNotifications(any());
        verify(notificationService, never()).send(any());
    }

    @Test
    public void testHandlePendingNotification_NotificationIntervalDisabled() {
        // arrange
        String userId = "user1";
        NotificationEvent event = NotificationEvent.builder().receiverId(userId).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        Map<String, UserData> userDataMap = Map.of(
                userId, mock(UserData.class)
        );

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId).getNotificationInterval(event)).thenReturn(NotificationInterval.disabled);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.disabled);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager).saveAllNotifications(argThat((List<NotificationEvent> events) -> events.stream().allMatch(e -> e.getStatus() == Status.IGNORED)));
        verify(notificationService, never()).send(any());
    }

    @Test
    public void testHandlePendingNotification_NotificationIntervalImmediate() {
        // arrange
        String userId = "user1";
        NotificationEvent event = NotificationEvent.builder().receiverId(userId).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        Map<String, UserData> userDataMap = Map.of(
                userId, mock(UserData.class)
        );

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId).getNotificationInterval(event)).thenReturn(NotificationInterval.immediately);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.immediately);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, times(1)).saveAllNotifications(notificationEvents);
        verify(notificationService, times(1)).send(notificationEvents);
    }

    @Test
    public void testHandlePendingNotification_NotificationIntervalDaily() {
        // arrange
        String userId = "user1";
        NotificationEvent event = NotificationEvent.builder().receiverId(userId).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        Map<String, UserData> userDataMap = Map.of(
                userId, mock(UserData.class)
        );

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId).getNotificationInterval(event)).thenReturn(NotificationInterval.daily);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.daily);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, times(1)).saveAllNotifications(notificationEvents);
        verify(notificationService, times(1)).send(notificationEvents);
    }

    @Test
    public void testHandlePendingNotification_NotificationIntervalWeekly() {
        // arrange
        String userId = "user1";
        NotificationEvent event = NotificationEvent.builder().receiverId(userId).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        Map<String, UserData> userDataMap = Map.of(
                userId, mock(UserData.class)
        );

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId).getNotificationInterval(event)).thenReturn(NotificationInterval.weekly);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.weekly);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, times(1)).saveAllNotifications(notificationEvents);
        verify(notificationService, times(1)).send(notificationEvents);
    }

    @Test
    public void testHandlePendingNotification_MixedIntervals() {
        // arrange
        String userId1 = "user1";
        String userId2 = "user2";
        String userId3 = "user3";
        String userId4 = "user4";

        NotificationEvent event1 = NotificationEvent.builder().receiverId(userId1).build();
        NotificationEvent event2 = NotificationEvent.builder().receiverId(userId2).build();
        NotificationEvent event3 = NotificationEvent.builder().receiverId(userId3).build();
        NotificationEvent event4 = NotificationEvent.builder().receiverId(userId4).build();

        List<NotificationEvent> notificationEvents = List.of(event1, event2, event3, event4);

        Map<String, UserData> userDataMap = Map.of(
                userId1, mock(UserData.class),
                userId2, mock(UserData.class),
                userId3, mock(UserData.class),
                userId4, mock(UserData.class)
        );

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);

        when(userDataMap.get(userId1).getNotificationInterval(event1)).thenReturn(NotificationInterval.immediately);
        when(userDataMap.get(userId2).getNotificationInterval(event2)).thenReturn(NotificationInterval.daily);
        when(userDataMap.get(userId3).getNotificationInterval(event3)).thenReturn(NotificationInterval.weekly);
        when(userDataMap.get(userId4).getNotificationInterval(event4)).thenReturn(NotificationInterval.disabled);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.immediately);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager).saveAllNotifications(argThat((List<NotificationEvent> events) ->
                events.stream().anyMatch(e -> e.getStatus() == Status.IGNORED)));

        verify(notificationService).send(argThat((List<NotificationEvent> events) ->
                events.stream().anyMatch(e -> e.getReceiverId().equals(userId1) || e.getReceiverId().equals(userId2) || e.getReceiverId().equals(userId3))));
    }

    @Test
    public void testHandlePendingNotification_NoNotificationsMatchInterval() {
        // arrange
        String userId = "user1";
        NotificationEvent event = NotificationEvent.builder().receiverId(userId).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        Map<String, UserData> userDataMap = Map.of(
                userId, mock(UserData.class)
        );

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId).getNotificationInterval(event)).thenReturn(NotificationInterval.daily);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.weekly);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, never()).saveAllNotifications(any());
        verify(notificationService, never()).send(any());
    }

    @Test
    public void testHandlePendingNotification_NotificationsWithSpecificIntervalOnly() {
        // arrange
        String userId1 = "user1";
        String userId2 = "user2";

        NotificationEvent event1 = NotificationEvent.builder().receiverId(userId1).build();
        NotificationEvent event2 = NotificationEvent.builder().receiverId(userId2).build();
        List<NotificationEvent> notificationEvents = List.of(event1, event2);

        Map<String, UserData> userDataMap = Map.of(
                userId1, mock(UserData.class),
                userId2, mock(UserData.class)
        );

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId1).getNotificationInterval(event1)).thenReturn(NotificationInterval.weekly);
        when(userDataMap.get(userId2).getNotificationInterval(event2)).thenReturn(NotificationInterval.daily);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.weekly);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, times(1)).saveAllNotifications(argThat((List<NotificationEvent> events) ->
                events.stream().allMatch(e -> e.getReceiverId().equals(userId1))));
        verify(notificationService, times(1)).send(argThat((List<NotificationEvent> events) ->
                events.stream().allMatch(e -> e.getReceiverId().equals(userId1))));
    }

    @Test
    public void testHandlePendingNotification_AllNotificationsIgnoredForDisabledInterval() {
        // arrange
        String userId1 = "user1";
        String userId2 = "user2";

        NotificationEvent event1 = NotificationEvent.builder().receiverId(userId1).build();
        NotificationEvent event2 = NotificationEvent.builder().receiverId(userId2).build();
        List<NotificationEvent> notificationEvents = List.of(event1, event2);

        Map<String, UserData> userDataMap = Map.of(
                userId1, mock(UserData.class),
                userId2, mock(UserData.class)
        );

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId1).getNotificationInterval(event1)).thenReturn(NotificationInterval.disabled);
        when(userDataMap.get(userId2).getNotificationInterval(event2)).thenReturn(NotificationInterval.disabled);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.disabled);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, times(1)).saveAllNotifications(argThat((List<NotificationEvent> events) ->
                events.stream().allMatch(e -> e.getStatus() == Status.IGNORED)));
        verify(notificationService, never()).send(any());
    }

    @Test
    public void testHandlePendingNotification_NoUserDataAvailable() {
        // arrange
        String userId = "user1";
        NotificationEvent event = NotificationEvent.builder().receiverId(userId).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(Collections.emptyMap());

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.immediately);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager).saveAllNotifications(argThat((List<NotificationEvent> events) ->
                events.stream().allMatch(e -> e.getStatus() == Status.IGNORED)));
        verify(notificationService, never()).send(any());
    }

    @Test
    public void testHandlePendingNotification_AllEventsIgnored() {
        // arrange
        String userId1 = "user1";
        String userId2 = "user2";

        NotificationEvent event1 = NotificationEvent.builder().receiverId(userId1).build();
        NotificationEvent event2 = NotificationEvent.builder().receiverId(userId2).build();
        List<NotificationEvent> notificationEvents = List.of(event1, event2);

        Map<String, UserData> userDataMap = Map.of(
                userId1, mock(UserData.class),
                userId2, mock(UserData.class)
        );

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId1).getNotificationInterval(event1)).thenReturn(NotificationInterval.disabled);
        when(userDataMap.get(userId2).getNotificationInterval(event2)).thenReturn(NotificationInterval.disabled);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.immediately);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager, times(1)).saveAllNotifications(argThat((List<NotificationEvent> events) ->
                events.stream().allMatch(e -> e.getStatus() == Status.IGNORED)));
        verify(notificationService, never()).send(any());
    }

    @Test
    public void testHandlePendingNotification_DuplicatesWithDisabledInterval() {
        // arrange
        String userId1 = "user1";

        NotificationEvent event1 = NotificationEvent.builder().receiverId(userId1).build();
        NotificationEvent event2 = NotificationEvent.builder().receiverId(userId1).build();
        List<NotificationEvent> notificationEvents = List.of(event1, event2);

        Map<String, UserData> userDataMap = Map.of(
                userId1, mock(UserData.class)
        );

        when(notificationManager.getAllNotifications(Status.PENDING)).thenReturn(notificationEvents);
        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId1).getNotificationInterval(any())).thenReturn(NotificationInterval.disabled);

        // act
        notificationHandler.handlePendingNotification(NotificationInterval.disabled);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(Status.PENDING);
        verify(notificationManager).saveAllNotifications(argThat((List<NotificationEvent> events) ->
                events.stream().allMatch(e -> e.getStatus() == Status.IGNORED)));
        verify(notificationService, never()).send(any());
    }

    @Test
    public void testHandleIncomingNotifications_AllNewNotifications() {
        // arrange
        String userId1 = "user1";
        String userId2 = "user2";

        NotificationEvent event1 = NotificationEvent.builder().receiverId(userId1).status(Status.NEW).build();
        NotificationEvent event2 = NotificationEvent.builder().receiverId(userId2).status(Status.NEW).build();
        List<NotificationEvent> notificationEvents = List.of(event1, event2);

        Map<String, UserData> userDataMap = Map.of(
                userId1, mock(UserData.class),
                userId2, mock(UserData.class)
        );

        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId1).getNotificationInterval(any())).thenReturn(NotificationInterval.immediately);
        when(userDataMap.get(userId2).getNotificationInterval(any())).thenReturn(NotificationInterval.immediately);

        // act
        notificationHandler.handleIncomingNotifications(notificationEvents);

        // assert
        verify(notificationManager, times(3)).saveAllNotifications(notificationEvents);
        verify(notificationService, times(1)).send(notificationEvents);
    }

    @Test
    public void testHandleIncomingNotifications_NoNotificationServicesAvailable() {
        // arrange
        NotificationHandler handlerWithNoServices = new NotificationHandler(notificationManager, Collections.emptyList(), userDataService);
        String userId1 = "user1";
        NotificationEvent event = NotificationEvent.builder().receiverId(userId1).status(Status.NEW).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        Map<String, UserData> userDataMap = Map.of(
                userId1, mock(UserData.class)
        );

        when(userDataService.getUserDataAsMap(anyList())).thenReturn(userDataMap);
        when(userDataMap.get(userId1).getNotificationInterval(any())).thenReturn(NotificationInterval.immediately);

        // act
        handlerWithNoServices.handleIncomingNotifications(notificationEvents);

        // assert
        verify(notificationManager, times(3)).saveAllNotifications(notificationEvents);
        verify(notificationService, never()).send(any());
    }

    @Test
    public void testHandlePendingNotificationOnAddedUser() {
        // arrange
        String userId1 = "user1";
        UserData userData = mock(UserData.class);
        NotificationEvent event = NotificationEvent.builder().receiverId(userId1).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        when(userData.getId()).thenReturn(userId1);
        when(notificationManager.getAllNotifications(userId1, List.of(Status.PENDING))).thenReturn(notificationEvents);
        when(userData.getNotificationInterval(event)).thenReturn(NotificationInterval.immediately);

        UserDataAddedEvent addedEvent = new UserDataAddedEvent(userData);

        // act
        notificationHandler.handlePendingNotificationOnAddedUser(addedEvent);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(userId1, List.of(Status.PENDING));
        verify(notificationService, times(1)).send(notificationEvents);
        verify(notificationManager, times(1)).saveAllNotifications(notificationEvents);
    }

    @Test
    public void testHandlePendingNotificationOnDeletedUser() {
        // arrange
        String userId = "user1";
        UserData userData = mock(UserData.class);
        NotificationEvent event = NotificationEvent.builder().receiverId(userId).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        when(userData.getId()).thenReturn(userId);
        when(notificationManager.getAllNotifications(userId, List.of(Status.PENDING))).thenReturn(notificationEvents);

        UserDataDeletedEvent deletedEvent = new UserDataDeletedEvent(userData);

        // act
        notificationHandler.handlePendingNotificationOnDeletedUser(deletedEvent);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(userId, List.of(Status.PENDING));
        verify(notificationManager, times(1)).saveAllNotifications(argThat((List<NotificationEvent> events) ->
                events.stream().allMatch(e -> e.getStatus() == Status.IGNORED)));
    }

    @Test
    public void testHandlePendingNotificationOnChangedUser_NotificationInterval_Immediately() {
        // arrange
        String userId = "user1";
        UserData oldUserData = mock(UserData.class);
        UserData newUserData = mock(UserData.class);
        NotificationEvent event = NotificationEvent.builder().receiverId(userId).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        when(oldUserData.getId()).thenReturn(userId);
        when(newUserData.getId()).thenReturn(userId);
        when(notificationManager.getAllNotifications(userId, List.of(Status.PENDING))).thenReturn(notificationEvents);
        when(oldUserData.getNotificationInterval(event)).thenReturn(NotificationInterval.daily);
        when(newUserData.getNotificationInterval(event)).thenReturn(NotificationInterval.immediately);

        UserDataChangedEvent changedEvent = new UserDataChangedEvent(oldUserData, newUserData);

        // act
        notificationHandler.handlePendingNotificationOnChangedUser(changedEvent);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(userId, List.of(Status.PENDING));
        verify(notificationService, times(1)).send(notificationEvents);
        verify(notificationManager, times(1)).saveAllNotifications(notificationEvents);
    }

    @Test
    public void testHandlePendingNotificationOnChangedUser_NotificationInterval_Disabled() {
        // arrange
        String userId = "user1";
        UserData oldUserData = mock(UserData.class);
        UserData newUserData = mock(UserData.class);
        NotificationEvent event = NotificationEvent.builder().receiverId(userId).build();
        List<NotificationEvent> notificationEvents = List.of(event);

        when(oldUserData.getId()).thenReturn(userId);
        when(newUserData.getId()).thenReturn(userId);
        when(notificationManager.getAllNotifications(userId, List.of(Status.PENDING))).thenReturn(notificationEvents);
        when(oldUserData.getNotificationInterval(event)).thenReturn(NotificationInterval.daily);
        when(newUserData.getNotificationInterval(event)).thenReturn(NotificationInterval.disabled);

        UserDataChangedEvent changedEvent = new UserDataChangedEvent(oldUserData, newUserData);

        // act
        notificationHandler.handlePendingNotificationOnChangedUser(changedEvent);

        // assert
        verify(notificationManager, times(1)).getAllNotifications(userId, List.of(Status.PENDING));
        verify(notificationService, never()).send(any());
        verify(notificationManager, times(1)).saveAllNotifications(notificationEvents);
        verify(notificationManager).saveAllNotifications(argThat((List<NotificationEvent> events) -> events.stream().allMatch(e -> e.getStatus() == Status.IGNORED)));

    }
}
