package org.edu_sharing.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.kafka.notification.events.NodeBaseEventDTO;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("NodeIssueMessage")
@Document(collation = "notification")
public class NodeIssueEvent extends NodeBaseEvent {
    private  String reason;
    private  String userComment;
}