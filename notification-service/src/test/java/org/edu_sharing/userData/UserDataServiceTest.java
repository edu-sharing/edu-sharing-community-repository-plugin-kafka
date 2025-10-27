package org.edu_sharing.userData;

import org.edu_sharing.kafka.user.NotificationIntervalDTO;
import org.edu_sharing.kafka.user.UserDataDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDataServiceTest {

    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<List<UserData>> saveAllCaptor;

    @Captor
    private ArgumentCaptor<UserData> deleteCaptor;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    private UserDataService underTest;

    @BeforeEach
    void setup() {
        underTest = new UserDataService(userDataRepository, eventPublisher);
    }

    private static Stream<Arguments> setUserDataTestParameter() {

        List<String> keys = List.of("lenny", "william", "johansson");
        List<UserDataDTO> newUserDataDto = List.of(
                new UserDataDTO("Lenny", "Linux", "lenny.linux@example.com", "de_DE", NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately),
                new UserDataDTO("William", "Windows", "william.windows@example.com", "de_DE", NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately),
                new UserDataDTO("Scala", "Johansson", "scala.johansson@example.com", "de_DE", NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately, NotificationIntervalDTO.immediately)
        );

        List<UserData> newUserData = IntStream.range(0, keys.size())
                .mapToObj(i -> UserDataService.createUserData(keys.get(i), newUserDataDto.get(i)))
                .toList();

        List<UserData> existingUserData = List.of(
                new UserData("lenny", "Lenny", "Linux", List.of("lenny.linux@example.com"), "de_DE", NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily),
                new UserData("william", "William", "Windows", List.of("william.windows@example.com"), "de_DE", NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily),
                new UserData("johansson", "Scala", "Johansson", List.of("scala.johansson@example.com"), "de_DE", NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily, NotificationInterval.daily)
        );

        Arguments addAllArgs = createArgumentSet(keys, newUserDataDto, List.of(), newUserData, List.of(), newUserData.stream().map(UserDataAddedEvent::new).map(x -> (Object) x).toList());
        Arguments changeAllArgs = createArgumentSet(keys, newUserDataDto, existingUserData, newUserData, List.of(), IntStream.range(0, newUserData.size()).mapToObj(i -> new UserDataChangedEvent(existingUserData.get(i), newUserData.get(i))).map(x -> (Object) x).toList());
        Arguments deleteAllArgs = createArgumentSet(keys, newUserDataDto.stream().map(x -> (UserDataDTO) null).toList(), existingUserData, List.of(), existingUserData, existingUserData.stream().map(UserDataDeletedEvent::new).map(x -> (Object) x).toList());
        return Stream.of(addAllArgs, changeAllArgs, deleteAllArgs);
    }

    private static Arguments createArgumentSet(List<String> keys, List<UserDataDTO> userDataDtoList, List<UserData> existingUserData, List<UserData> expectedSaves, List<UserData> expectedDeletions, List<Object> expectedEvents) {
        return Arguments.of(keys, userDataDtoList, existingUserData, expectedSaves, expectedDeletions, expectedEvents);
    }

    @ParameterizedTest
    @MethodSource("setUserDataTestParameter")
    void setUserData_test(List<String> keys, List<UserDataDTO> messages, List<UserData> existingUserData, List<UserData> expectedSaves, List<UserData> expectedDeletions, List<Object> expectedEvents) {
        // arrange
        lenient().when(userDataRepository.findAllById(keys)).thenReturn(existingUserData);
        existingUserData.forEach(x -> lenient().when(userDataRepository.findById(x.getId())).thenReturn(Optional.of(x)));

        // act
        underTest.setUserData(keys, messages);

        // assert
        verify(userDataRepository, expectedSaves.isEmpty() ? never() : times(1)).saveAll(saveAllCaptor.capture());
        verify(userDataRepository, expectedDeletions.isEmpty() ? never() : times(expectedDeletions.size())).delete(deleteCaptor.capture());
        verify(eventPublisher, expectedEvents.isEmpty() ? never() : times(expectedEvents.size())).publishEvent(eventCaptor.capture());

        List<List<UserData>> savedData = saveAllCaptor.getAllValues();
        assertEquals(expectedSaves.isEmpty() ? 0 : 1, savedData.size(), "Expected exactly one save call");
        assertArrayEquals(expectedSaves.toArray(), savedData.stream().findFirst().orElse(List.of()).toArray(), "Expected saved data to be equal to expected data");

        List<UserData> deletedData = deleteCaptor.getAllValues();
        assertArrayEquals(expectedDeletions.toArray(), deletedData.toArray(), "Expected deleted data to be equal to expected data");

        List<Object> events = eventCaptor.getAllValues();
        assertArrayEquals(expectedEvents.toArray(), events.toArray(), "Expected events to be equal to expected events");

    }
}
