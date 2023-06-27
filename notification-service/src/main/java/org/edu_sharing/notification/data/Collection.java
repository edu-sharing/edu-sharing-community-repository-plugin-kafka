package org.edu_sharing.notification.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Collection extends NodeData {
}
