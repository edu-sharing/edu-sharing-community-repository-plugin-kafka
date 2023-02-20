package org.edu_sharing.service;

import lombok.AllArgsConstructor;
import org.edu_sharing.UpdateUserSettingsRequest;
import org.edu_sharing.domain.UserSettings;
import org.edu_sharing.repository.UserSettingsRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;


    @Cacheable(value = "userSettings", key = "#userId")
    public Optional<UserSettings> getUserSetting(String userId) {
        return userSettingsRepository.findById(userId);
    }

    @CacheEvict(value = "userSettings", key="#userSettingsRequest.userId()")
    public void updateUserSettings(UpdateUserSettingsRequest userSettingsRequest) {
        UserSettings userSettings = new UserSettings(
                userSettingsRequest.userId(),
                userSettingsRequest.emailAddress(),
                userSettingsRequest.disabledMessageTypes());

        // TODO check userId check
        // TODO check email address check
        userSettingsRepository.save(userSettings);
    }
}
