package org.edu_sharing.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettings {
    @Id
    private String userId;
    private String emailAddress;

    List<String> disabledMessageTypes;
}
