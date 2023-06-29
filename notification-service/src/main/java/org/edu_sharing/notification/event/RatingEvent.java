package org.edu_sharing.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.edu_sharing.notification.data.NodeData;
import org.edu_sharing.notification.data.Status;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("RatingEvent")
@Document(collection = "notification")
public class RatingEvent extends NodeBaseEvent {

    public RatingEvent(String id, Date timestamp, String creatorId, String receiverId, Status status, NodeData node, double newRating, double ratingSum, long ratingCount) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.newRating = newRating;
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
    }

    /**
     * the new rating that was given
     */
    private double newRating;
    /**
     * the new rating sum
     */
    private double ratingSum;
    /**
     * the count of ratings in total
     * (To get the avg use ratingSum / ratingCount)
     */
    private long ratingCount;
}