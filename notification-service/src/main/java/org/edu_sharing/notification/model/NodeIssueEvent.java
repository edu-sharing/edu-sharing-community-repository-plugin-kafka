package org.edu_sharing.notification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("NodeIssueEvent")
@Document(collation = "notification")
public class NodeIssueEvent extends NodeBaseEvent {
    private String email;
    private String reason;
    private String userComment;
}