package org.edu_sharing;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public record UpdateUserSettingsRequest (
        String userId,
        String emailAddress,
        List<String> disabledMessageTypes
){ }
