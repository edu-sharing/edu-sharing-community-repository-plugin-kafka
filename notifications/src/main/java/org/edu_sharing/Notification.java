package org.edu_sharing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Notification {
    private String issue;
    private String message;
}
