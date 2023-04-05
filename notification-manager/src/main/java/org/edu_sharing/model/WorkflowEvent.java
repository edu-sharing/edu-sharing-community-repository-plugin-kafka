package org.edu_sharing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("WorkflowMessage")
@Document(collation = "notification")
public class WorkflowEvent extends NodeBaseEvent {
    private String workflowStatus;
    private  String userComment;
}