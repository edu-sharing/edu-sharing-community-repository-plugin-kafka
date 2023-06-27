package org.edu_sharing.userData;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.edu_sharing.kafka.user.UserDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserDataService {

    private final UserDataRepository userDataRepository;


    @Value("${spring.mail.send.address}")
    private String mailSendAddress;

    @Value("${spring.mail.report.address}")
    private String mailReportAddress;

    @Value("${spring.application.name}")
    private String applicationName;

    public void setUserData(List<String> keys, List<UserDataDTO> message) {
        userDataRepository.saveAll(zip(keys, message.stream().map(this::tranform).toList(),
                (key, data) -> {
                    data.setId(key);
                    return data;
                }).toList());
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
        if(ids.remove("system")){
            userData.put("system",new UserData("system", "", applicationName, mailSendAddress, "de-DE"));
        }

        if(ids.remove("report")){
            userData.put("report", new UserData("report", "", applicationName, mailReportAddress, "de-DE"));
        }

        return userData;
    }


    public static <R, T, U> Stream<R> zip(List<T> list1, List<U> list2, BiFunction<? super T, ? super U, ? extends R> join) {
        int size = Math.min(list1.size(), list2.size());

        return IntStream.range(0, size)
                .mapToObj(i -> join.apply(list1.get(i), list2.get(i)));
    }


    private UserData tranform(UserDataDTO userDataDTO) {
        return new UserData(
                null,
                userDataDTO.getFirstName(),
                userDataDTO.getLastName(),
                userDataDTO.getEmail(),
                userDataDTO.getLocale(),
                NotificationInterval.valueOf(userDataDTO.getAddToCollectionEvent().toString()),
                 NotificationInterval.valueOf(userDataDTO.getCommentEvent().toString()),
                 NotificationInterval.valueOf(userDataDTO.getInviteEvent().toString()),
                 NotificationInterval.valueOf(userDataDTO.getNodeIssueEvent().toString()),
                 NotificationInterval.valueOf(userDataDTO.getRatingEvent().toString()),
                 NotificationInterval.valueOf(userDataDTO.getWorkflowEvent().toString()),
                 NotificationInterval.valueOf(userDataDTO.getMetadataSuggestionEvent().toString())
        );

    }
}
