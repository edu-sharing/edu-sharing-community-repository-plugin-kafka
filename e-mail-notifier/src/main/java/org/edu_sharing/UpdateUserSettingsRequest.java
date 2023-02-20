package org.edu_sharing;

import java.util.List;

public record UpdateUserSettingsRequest(
        String userId,
        String emailAddress,
        List<String> disabledMessageTypes
) {
}
