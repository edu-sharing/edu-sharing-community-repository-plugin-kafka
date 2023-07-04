package org.edu_sharing.notification.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@SuperBuilder
@AllArgsConstructor
public class Permission {
    private String permission;
    private String de;
}
