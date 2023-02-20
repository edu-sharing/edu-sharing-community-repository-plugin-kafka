package org.edu_sharing;

import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.UpdateUserSettingsRequest;
import org.edu_sharing.service.UserSettingsService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/customer")
public record UserSettingsController(
        UserSettingsService userSettingsService
) {


    @PutMapping
    public void updateUserSettings(@RequestBody UpdateUserSettingsRequest userSettingsRequest){
        log.info("Update user settings {}", userSettingsRequest);
        userSettingsService.updateUserSettings(userSettingsRequest);
    }
}
