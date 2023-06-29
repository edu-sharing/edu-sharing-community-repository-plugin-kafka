package org.edu_sharing.notification.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import java.util.List;
import java.util.Map;

@Data
@Jacksonized
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Collection extends NodeData {
    public Collection(String type, List<String> aspects, Map<String, Object> properties) {
        super(type, aspects, properties);
    }
}
