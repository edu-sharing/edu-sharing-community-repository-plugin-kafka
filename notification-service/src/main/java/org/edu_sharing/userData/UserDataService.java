package org.edu_sharing.userData;

import lombok.RequiredArgsConstructor;
import org.edu_sharing.kafka.user.UserDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserDataService {

    private final UserDataRepository userDataRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Value("${spring.mail.send.address}")
    private String mailSendAddress;

    @Value("${spring.mail.report.address}")
    private String mailReportAddress;

    @Value("${spring.application.name}")
    private String applicationName;

    public void setUserData(List<String> keys, List<UserDataDTO> messages) {
        List<UserData> newUserDataList = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            Optional<UserDataDTO> message = Optional.ofNullable(messages.get(i));
            UserData userData = createUserData(keys.get(i), message);
            if (message.isEmpty()) {
                userDataRepository.findById(keys.get(i))
                        .ifPresent(x -> {
                            userDataRepository.delete(x);
                            eventPublisher.publishEvent(new UserDataDeletedEvent(x));
                        });
            } else {
                newUserDataList.add(userData);
            }
        }

        if (newUserDataList.isEmpty()) {
            return;
        }

        Map<String, UserData> oldUserDataMap = userDataRepository.findAllById(keys)
                .stream()
                .collect(Collectors.toMap(UserData::getId, x -> x));

        userDataRepository.saveAll(newUserDataList);

        for (UserData newUserData : newUserDataList) {
            UserData oldUserData = oldUserDataMap.get(newUserData.getId());
            if (oldUserData == null) {
                eventPublisher.publishEvent(new UserDataAddedEvent(newUserData));
            } else if (!oldUserData.equals(newUserData)) {
                eventPublisher.publishEvent(new UserDataChangedEvent(oldUserData, newUserData));
            }

        }
    }

    public Optional<UserData> getUserData(String id) {
        if ("system".equals(id)) {
            return Optional.of(new UserData("system", "", applicationName, mailSendAddress, "de-DE"));
        }

        if ("report".equals(id)) {
            return Optional.of(new UserData("report", "", applicationName, mailReportAddress, "de-DE"));
        }

        return userDataRepository.findById(id);
    }


    public Map<String, UserData> getUserDataAsMap(List<String> ids) {
        Map<String, UserData> userData = userDataRepository.findByIdInAsMap(ids);
        if (ids.remove("system")) {
            userData.put("system", new UserData("system", "", applicationName, mailSendAddress, "de-DE"));
        }

        if (ids.remove("report")) {
            userData.put("report", new UserData("report", "", applicationName, mailReportAddress, "de-DE"));
        }

        return userData;
    }


    public static <R, T, U> Stream<R> zip(List<T> list1, List<U> list2, BiFunction<? super T, ? super U, ? extends R> join) {
        int size = Math.min(list1.size(), list2.size());

        return IntStream.range(0, size)
                .mapToObj(i -> join.apply(list1.get(i), list2.get(i)));
    }


    public static UserData createUserData(String id, UserDataDTO userDataDTO) {
        return createUserData(id, Optional.ofNullable(userDataDTO));
    }

    private static UserData createUserData(String id, Optional<UserDataDTO> userDataDTO) {
        return new UserData(
                id,
                userDataDTO.map(UserDataDTO::getFirstName).orElse(null),
                userDataDTO.map(UserDataDTO::getLastName).orElse(null),
                userDataDTO.map(UserDataDTO::getEmail).orElse(null),
                userDataDTO.map(UserDataDTO::getLocale).orElse(null),
                userDataDTO.map(UserDataDTO::getAddToCollectionEvent).map(Object::toString).map(NotificationInterval::valueOf).orElse(null),
                userDataDTO.map(UserDataDTO::getProposeForCollectionEvent).map(Object::toString).map(NotificationInterval::valueOf).orElse(null),
                userDataDTO.map(UserDataDTO::getCommentEvent).map(Object::toString).map(NotificationInterval::valueOf).orElse(null),
                userDataDTO.map(UserDataDTO::getInviteEvent).map(Object::toString).map(NotificationInterval::valueOf).orElse(null),
                userDataDTO.map(UserDataDTO::getNodeIssueEvent).map(Object::toString).map(NotificationInterval::valueOf).orElse(null),
                userDataDTO.map(UserDataDTO::getRatingEvent).map(Object::toString).map(NotificationInterval::valueOf).orElse(null),
                userDataDTO.map(UserDataDTO::getWorkflowEvent).map(Object::toString).map(NotificationInterval::valueOf).orElse(null),
                userDataDTO.map(UserDataDTO::getMetadataSuggestionEvent).map(Object::toString).map(NotificationInterval::valueOf).orElse(null)
        );
    }
}
