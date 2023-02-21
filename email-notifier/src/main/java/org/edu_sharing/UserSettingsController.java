package org.edu_sharing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.service.UserSettingsService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/customer")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;


    @PutMapping
    public void updateUserSettings(@RequestBody UpdateUserSettingsRequest userSettingsRequest){
        log.info("Update user settings {}", userSettingsRequest);
        userSettingsService.updateUserSettings(userSettingsRequest);
    }
}
